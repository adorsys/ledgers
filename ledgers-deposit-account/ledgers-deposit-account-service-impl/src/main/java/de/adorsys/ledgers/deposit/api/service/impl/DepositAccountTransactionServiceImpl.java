/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.PostingMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.SerializeService;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.util.exception.DepositErrorCode.DEPOSIT_ACCOUNT_NOT_FOUND;
import static de.adorsys.ledgers.util.exception.DepositErrorCode.DEPOSIT_OPERATION_FAILURE;
import static java.lang.String.format;

@Service
public class DepositAccountTransactionServiceImpl extends AbstractServiceImpl implements DepositAccountTransactionService {
    private final PaymentMapper paymentMapper;
    private final PostingMapper postingMapper;
    private final PostingService postingService;
    private final SerializeService serializeService;
    private final DepositAccountService depositAccountService;
    private final CurrencyExchangeRatesService exchangeRatesService;
    private final ObjectMapper objectMapper;

    public DepositAccountTransactionServiceImpl(PostingService postingService, LedgerService ledgerService, DepositAccountConfigService depositAccountConfigService, PaymentMapper paymentMapper, PostingMapper postingMapper, SerializeService serializeService, DepositAccountService depositAccountService, CurrencyExchangeRatesService exchangeRatesService, ObjectMapper objectMapper) {
        super(depositAccountConfigService, ledgerService);
        this.postingService = postingService;
        this.paymentMapper = paymentMapper;
        this.postingMapper = postingMapper;
        this.serializeService = serializeService;
        this.depositAccountService = depositAccountService;
        this.exchangeRatesService = exchangeRatesService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void depositCash(String accountId, AmountBO amount, String recordUser) {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_OPERATION_FAILURE)
                          .devMsg("Deposited amount must be greater than zero")
                          .build();
        }

        DepositAccountDetailsBO depositAccount = depositAccountService.getAccountDetailsById(accountId, LocalDateTime.now(), true);

        if (!depositAccount.isEnabled()) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_OPERATION_FAILURE)
                          .devMsg("Deposit account is blocked, cannot deposit cash.")
                          .build();
        }

        Currency accountCurrency = depositAccount.getAccount().getCurrency();

        if (!accountCurrency.equals(amount.getCurrency())) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_OPERATION_FAILURE)
                          .devMsg(format("Deposited amount and account currencies are different. Requested currency: %s, Account currency: %s",
                                         amount.getCurrency().getCurrencyCode(), accountCurrency))
                          .build();
        }
        LedgerBO ledger = loadLedger();
        LocalDateTime postingDateTime = LocalDateTime.now();
        depositCash(depositAccount, amount, recordUser, ledger, postingDateTime);
    }

    /**
     * Execute a payment. This principally applies to:
     * - Single payment
     * - Future date payment
     * - Periodic Payment
     * - Bulk Payment with batch execution
     * + Bulk payment without batch execution will be split into single payments
     * and each single payment will be individually sent to this method.
     */
    @Override
    public void bookPayment(PaymentBO payment, LocalDateTime pstTime, String userName) {
        String oprDetails = serializeService.serializeOprDetails(paymentMapper.toPaymentOrder(payment));
        LedgerBO ledger = loadLedger();

        if (payment.getPaymentType() == PaymentTypeBO.BULK && Optional.ofNullable(payment.getBatchBookingPreferred()).orElse(false)) {
            createBatchPostings(pstTime, oprDetails, ledger, payment, userName);
        } else {
            createRegularPostings(pstTime, oprDetails, ledger, payment, userName);
        }
    }

    private void depositCash(DepositAccountDetailsBO depositAccount, AmountBO amount, String recordUser, LedgerBO ledger, LocalDateTime postingDateTime) {
        PostingBO posting = postingMapper.buildPosting(postingDateTime, Ids.id(), "ATM Cash Deposit", ledger, recordUser);
        PostingLineBO debitLine = composeLine(depositAccount, amount, ledger, postingDateTime, true);
        PostingLineBO creditLine = composeLine(depositAccount, amount, ledger, postingDateTime, false);
        posting.getLines().addAll(Arrays.asList(debitLine, creditLine));
        postingService.newPosting(posting);
    }

    private PostingLineBO composeLine(DepositAccountDetailsBO depositAccount, AmountBO amount, LedgerBO ledger, LocalDateTime postingDateTime, boolean debit) {
        DepositAccountBO account = depositAccount.getAccount();
        LedgerAccountBO ledgerAccount = debit
                                                ? ledgerService.findLedgerAccount(ledger, depositAccountConfigService.getCashAccount())
                                                : ledgerService.findLedgerAccountById(account.getLinkedAccounts());

        BigDecimal debitAmount = getDCtAmount(amount, debit, null);
        BigDecimal creditAmount = getDCtAmount(amount, !debit, null);

        BalanceBO balanceAfterTransaction = resolveBalanceAfterTransaction(!debit, depositAccount, debitAmount);

        String lineId = Ids.id();
        AccountReferenceBO creditor = account.getReference();
        String debitTransactionDetails = serializeService.serializeOprDetails(paymentMapper.toDepositTransactionDetails(amount, account, creditor, postingDateTime.toLocalDate(), lineId, balanceAfterTransaction, objectMapper));
        return postingMapper.buildPostingLine(debitTransactionDetails, ledgerAccount, debitAmount, creditAmount, "ATM transfer", lineId);
    }

    private BalanceBO resolveBalanceAfterTransaction(boolean debit, DepositAccountDetailsBO details, BigDecimal amount) {
        Optional<BalanceBO> balanceAfterTransaction = Optional.ofNullable(details)
                                                              .map(DepositAccountDetailsBO::getBalances)
                                                              .map(this::getBalance);
        balanceAfterTransaction.ifPresent(b -> b.updateAmount(amount, debit ? BigDecimal::subtract : BigDecimal::add));
        return balanceAfterTransaction.orElse(null);
    }

    private BalanceBO resolveBalanceAfterTransactionForPayment(boolean debit, PaymentTargetBO target, BigDecimal amount) {
        DepositAccountDetailsBO accountDetails = debit
                                                         ? depositAccountService.getAccountDetailsById(target.getPayment().getAccountId(), LocalDateTime.now(), true)
                                                         : getAccount(target.getCreditorAccount().getIban(), target.getCreditorAccount().getCurrency());
        return resolveBalanceAfterTransaction(debit, accountDetails, amount);
    }

    private BalanceBO getBalance(List<BalanceBO> balances) {
        return balances.stream()
                       .filter(b -> b.getBalanceType() == BalanceTypeBO.INTERIM_AVAILABLE)
                       .findFirst()
                       .orElse(null);
    }

    private void createRegularPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, PaymentBO payment, String userName) {
        payment.getTargets().stream()
                .map(t -> {
                    t.setPayment(payment);
                    return buildDCPosting(pstTime, oprDetails, ledger, t, userName);
                }).forEach(postingService::newPosting);
    }

    private void createBatchPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, PaymentBO payment, String userName) {
        PostingBO posting = postingMapper.buildPosting(pstTime, payment.getPaymentId(), oprDetails, ledger, userName);
        List<ExchangeRateBO> ratesForDebitLine = new ArrayList<>();
        BigDecimal batchAmount = BigDecimal.ZERO;
        List<PostingLineBO> creditLines = new ArrayList<>();
        for (PaymentTargetBO t : payment.getTargets()) {
            t.setPayment(payment);
            List<ExchangeRateBO> rates = exchangeRatesService.getExchangeRates(payment.getDebtorAccount().getCurrency(), t.getInstructedAmount().getCurrency(), t.getCreditorAccount().getCurrency());
            Optional.ofNullable(resolveRateIfRequired(payment.getDebtorAccount(), rates)).ifPresent(ratesForDebitLine::add);
            PostingLineBO line = createLine(ledger, t, pstTime, rates, payment.getPaymentId(), false, true);
            creditLines.add(line);
            batchAmount = batchAmount.add(line.getCreditAmount());
            addAdditionalLinesIfRequired(t, ledger, pstTime, rates, posting.getOprId(), creditLines);
        }

        AmountBO amount = new AmountBO(payment.getDebtorAccount().getCurrency(), batchAmount);
        String id = Ids.id();
        BalanceBO balanceAfterTransaction = resolveBalanceAfterTransaction(true, depositAccountService.getAccountDetailsById(payment.getAccountId(), LocalDateTime.now(), true), batchAmount);
        ratesForDebitLine = ratesForDebitLine.isEmpty() ? null : ratesForDebitLine;
        String debitLineDetails = serializeService.serializeOprDetails(paymentMapper.toPaymentTargetDetailsBatch(id, payment, amount, pstTime.toLocalDate(), ratesForDebitLine, balanceAfterTransaction, objectMapper));
        LedgerAccountBO debtorLedgerAccount = getLedgerAccount(ledger, payment.getPaymentProduct(), payment.getDebtorAccount(), true, true, false);
        PostingLineBO debitLine = postingMapper.buildPostingLine(debitLineDetails, debtorLedgerAccount, amount.getAmount(), BigDecimal.ZERO, payment.getPaymentId(), id);
        posting.getLines().add(debitLine);
        posting.getLines().addAll(creditLines);
        postingService.newPosting(posting);
    }

    private PostingBO buildDCPosting(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, PaymentTargetBO target, String userName) {
        PostingBO posting = postingMapper.buildPosting(pstTime, target.getPayment().getPaymentId(), oprDetails, ledger, userName);
        List<ExchangeRateBO> rates = exchangeRatesService.getExchangeRates(target.getPayment().getDebtorAccount().getCurrency(), target.getInstructedAmount().getCurrency(), target.getCreditorAccount().getCurrency());
        PostingLineBO debitLine = createLine(ledger, target, pstTime, rates, posting.getOprId(), true, true);
        PostingLineBO creditLine = createLine(ledger, target, pstTime, rates, target.getPayment().getPaymentId(), false, true);
        posting.getLines().addAll(Arrays.asList(debitLine, creditLine));
        addAdditionalLinesIfRequired(target, ledger, pstTime, rates, posting.getOprId(), posting.getLines());
        return posting;
    }

    private void addAdditionalLinesIfRequired(PaymentTargetBO target, LedgerBO ledger, LocalDateTime pstTime, List<ExchangeRateBO> rates, String oprId, List<PostingLineBO> lines) {
        if (additionalLinesRequired(target)) {
            PostingLineBO additionalDebitLine = createLine(ledger, target, pstTime, rates, oprId, true, false);
            PostingLineBO additionalCreditLine = createLine(ledger, target, pstTime, rates, target.getPayment().getPaymentId(), false, false);
            lines.addAll(Arrays.asList(additionalDebitLine, additionalCreditLine));
        }
    }

    private boolean additionalLinesRequired(PaymentTargetBO target) {
        return !target.isAllCurrenciesMatch() && ledgerAccountId(target.getCreditorAccount()).isPresent();
    }

    // Here oprId is postingId for debitLine and paymentId for creditLine
    private PostingLineBO createLine(LedgerBO ledger, PaymentTargetBO target, LocalDateTime pstTime, List<ExchangeRateBO> rates, String oprId, boolean isDebitLine, boolean isFirstLine) {
        String id = Ids.id();
        ExchangeRateBO ratesForLine = resolveRateIfRequired(getReferenceByValue(target, isFirstLine), rates);

        LedgerAccountBO ledgerAccount = getLedgerAccount(ledger, target.getPayment().getPaymentProduct(), getReferenceByValue(target, isDebitLine), isDebitLine, isFirstLine, target.isAllCurrenciesMatch());
        BigDecimal debitAmount = getDCtAmount(target.getInstructedAmount(), isDebitLine, ratesForLine);
        BigDecimal creditAmount = getDCtAmount(target.getInstructedAmount(), !isDebitLine, ratesForLine);

        BalanceBO balanceAfterTransaction = resolveBalanceAfterTransactionForPayment(isDebitLine, target, isDebitLine ? debitAmount : creditAmount);
        String targetDetails = serializeService.serializeOprDetails(paymentMapper.toPaymentTargetDetails(id, target, pstTime.toLocalDate(), Optional.ofNullable(ratesForLine)
                                                                                                                                                    .map(Collections::singletonList)
                                                                                                                                                    .orElse(null), balanceAfterTransaction));
        return postingMapper.buildPostingLine(targetDetails, ledgerAccount, debitAmount, creditAmount, oprId, id);
    }

    private DepositAccountDetailsBO getAccount(String iban, Currency currency) {
        try {
            return depositAccountService.getAccountDetailsByIbanAndCurrency(iban, currency, LocalDateTime.now(), true);
        } catch (DepositModuleException e) {
            return null;
        }
    }

    private AccountReferenceBO getReferenceByValue(PaymentTargetBO target, boolean value) {
        return value
                       ? target.getPayment().getDebtorAccount()
                       : target.getCreditorAccount();
    }

    private ExchangeRateBO resolveRateIfRequired(AccountReferenceBO reference, List<ExchangeRateBO> rates) {
        return rates.stream()
                       .filter(r -> reference.getCurrency().equals(r.getCurrencyTo()))
                       .findFirst()
                       .orElse(null);
    }

    private BigDecimal getDCtAmount(AmountBO amount, boolean debit, ExchangeRateBO rate) {
        return debit
                       ? exchangeRatesService.applyRate(amount.getAmount(), rate)
                       : BigDecimal.ZERO;
    }

    private LedgerAccountBO getLedgerAccount(LedgerBO ledger, String paymentProduct, AccountReferenceBO reference, boolean isDebitLine, boolean isFirstLine, boolean isAllCurrenciesMatch) {
        if (isAllCurrenciesMatch) {
            return ledgerAccountId(reference)
                           .map(ledgerService::findLedgerAccountById)
                           .orElseGet(() -> loadClearingAccount(ledger, paymentProduct));
        } else if (isDebitLine && isFirstLine || !isDebitLine && !isFirstLine) {
            return ledgerAccountId(reference)
                           .map(ledgerService::findLedgerAccountById)
                           .orElseThrow(() -> DepositModuleException.builder()
                                                      .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                                                      .devMsg(format("Account with IBAN: %s not found", reference.getIban()))
                                                      .build());
        } else {
            return loadClearingAccount(ledger, paymentProduct);
        }
    }

    private Optional<String> ledgerAccountId(AccountReferenceBO reference) {
        return depositAccountService.getOptionalAccountByIbanAndCurrency(reference.getIban(), reference.getCurrency())
                       .map(DepositAccountBO::getLinkedAccounts);
    }
}
