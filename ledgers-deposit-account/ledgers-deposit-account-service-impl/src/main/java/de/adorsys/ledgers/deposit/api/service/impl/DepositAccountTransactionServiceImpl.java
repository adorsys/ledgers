/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

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
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public DepositAccountTransactionServiceImpl(PostingService postingService, LedgerService ledgerService, DepositAccountConfigService depositAccountConfigService, PaymentMapper paymentMapper, PostingMapper postingMapper, SerializeService serializeService, DepositAccountService depositAccountService, CurrencyExchangeRatesService exchangeRatesService) {
        super(depositAccountConfigService, ledgerService);
        this.postingService = postingService;
        this.paymentMapper = paymentMapper;
        this.postingMapper = postingMapper;
        this.serializeService = serializeService;
        this.depositAccountService = depositAccountService;
        this.exchangeRatesService = exchangeRatesService;
    }

    @Override
    public void depositCash(String accountId, AmountBO amount, String recordUser) {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_OPERATION_FAILURE)
                          .devMsg("Deposited amount must be greater than zero")
                          .build();
        }

        DepositAccountBO depositAccount = depositAccountService.getAccountDetailsById(accountId, LocalDateTime.now(), false).getAccount();

        if (!depositAccount.getCurrency().equals(amount.getCurrency())) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_OPERATION_FAILURE)
                          .devMsg(format("Deposited amount and account currencies are different. Requested currency: %s, Account currency: %s",
                                  amount.getCurrency().getCurrencyCode(), depositAccount.getCurrency()))
                          .build();
        }

        LedgerBO ledger = loadLedger();
        LocalDateTime postingDateTime = LocalDateTime.now();

        depositCash(depositAccount, amount, recordUser, ledger, postingDateTime);
    }

    private void depositCash(DepositAccountBO depositAccount, AmountBO amount, String recordUser, LedgerBO ledger, LocalDateTime postingDateTime) {
        PostingBO posting = postingMapper.buildPosting(postingDateTime, Ids.id(), "ATM Cash Deposit", ledger, recordUser);
        PostingLineBO debitLine = composeLine(depositAccount, amount, ledger, postingDateTime, true);
        PostingLineBO creditLine = composeLine(depositAccount, amount, ledger, postingDateTime, false);
        posting.getLines().addAll(Arrays.asList(debitLine, creditLine));
        postingService.newPosting(posting);
    }

    private PostingLineBO composeLine(DepositAccountBO depositAccount, AmountBO amount, LedgerBO ledger, LocalDateTime postingDateTime, boolean debit) {
        LedgerAccountBO account = debit
                                          ? ledgerService.findLedgerAccount(ledger, depositAccountConfigService.getCashAccount())
                                          : ledgerService.findLedgerAccountById(depositAccount.getLinkedAccounts());

        BigDecimal debitAmount = getDCtAmount(amount, debit, null);
        BigDecimal creditAmount = getDCtAmount(amount, !debit, null);

        String lineId = Ids.id();
        AccountReferenceBO creditor = depositAccount.getReference();
        String debitTransactionDetails = serializeService.serializeOprDetails(paymentMapper.toDepositTransactionDetails(amount, creditor, postingDateTime.toLocalDate(), lineId));
        return postingMapper.buildPostingLine(debitTransactionDetails, account, debitAmount, creditAmount, "ATM transfer", lineId);
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

        Set<PostingBO> postings = payment.getPaymentType() == PaymentTypeBO.BULK && Optional.ofNullable(payment.getBatchBookingPreferred()).orElse(false)
                                          ? createBatchPostings(pstTime, oprDetails, ledger, payment, userName)
                                          : createRegularPostings(pstTime, oprDetails, ledger, payment, userName);
        postings.forEach(postingService::newPosting);
    }

    private Set<PostingBO> createRegularPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, PaymentBO payment, String userName) {
        return payment.getTargets().stream()
                       .map(t -> {
                           t.setPayment(payment);
                           return buildDCPosting(pstTime, oprDetails, ledger, t, userName);
                       })
                       .collect(Collectors.toSet());
    }

    private Set<PostingBO> createBatchPostings(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, PaymentBO payment, String userName) {
        PostingBO posting = postingMapper.buildPosting(pstTime, payment.getPaymentId(), oprDetails, ledger, userName);
        List<ExchangeRateBO> ratesForDebitLine = new ArrayList<>();
        BigDecimal batchAmount = BigDecimal.ZERO;
        List<PostingLineBO> creditLines = new ArrayList<>();
        for (PaymentTargetBO t : payment.getTargets()) {
            t.setPayment(payment);
            List<ExchangeRateBO> rates = exchangeRatesService.getExchangeRates(payment.getDebtorAccount().getCurrency(), t.getInstructedAmount().getCurrency(), t.getCreditorAccount().getCurrency());
            ratesForDebitLine.add(resolveRateIfRequired(payment.getDebtorAccount(), rates));
            PostingLineBO line = createLine(ledger, t, pstTime, rates, payment.getPaymentId(), false, true);
            creditLines.add(line);
            batchAmount = batchAmount.add(line.getCreditAmount());
            if (additionalLinesRequired(t)) {
                PostingLineBO additionalCreditLine = createLine(ledger, t, pstTime, rates, t.getPayment().getPaymentId(), true, false);
                creditLines.add(additionalCreditLine);
            }
        }

        AmountBO amount = new AmountBO(payment.getDebtorAccount().getCurrency(), batchAmount);
        String id = Ids.id();
        String debitLineDetails = serializeService.serializeOprDetails(paymentMapper.toPaymentTargetDetailsBatch(id, payment, amount, pstTime.toLocalDate(), ratesForDebitLine));
        LedgerAccountBO debtorLedgerAccount = getLedgerAccount(ledger, payment.getTargets().iterator().next().getPaymentProduct(), payment.getDebtorAccount(), true, true, false);
        PostingLineBO debitLine = postingMapper.buildPostingLine(debitLineDetails, debtorLedgerAccount, amount.getAmount(), BigDecimal.ZERO, payment.getPaymentId(), id);
        posting.getLines().add(debitLine);
        posting.getLines().addAll(creditLines);

        return new HashSet<>(Collections.singletonList(posting));
    }

    private PostingBO buildDCPosting(LocalDateTime pstTime, String oprDetails, LedgerBO ledger, PaymentTargetBO target, String userName) {
        PostingBO posting = postingMapper.buildPosting(pstTime, target.getPayment().getPaymentId(), oprDetails, ledger, userName);
        List<ExchangeRateBO> rates = exchangeRatesService.getExchangeRates(target.getPayment().getDebtorAccount().getCurrency(), target.getInstructedAmount().getCurrency(), target.getCreditorAccount().getCurrency());
        PostingLineBO debitLine = createLine(ledger, target, pstTime, rates, posting.getOprId(), true, true);
        PostingLineBO creditLine = createLine(ledger, target, pstTime, rates, target.getPayment().getPaymentId(), false, true);
        posting.getLines().addAll(Arrays.asList(debitLine, creditLine));
        if (additionalLinesRequired(target)) {
            PostingLineBO additionalDebitLine = createLine(ledger, target, pstTime, rates, posting.getOprId(), true, false);
            PostingLineBO additionalCreditLine = createLine(ledger, target, pstTime, rates, target.getPayment().getPaymentId(), false, false);
            posting.getLines().addAll(Arrays.asList(additionalDebitLine, additionalCreditLine));
        }
        return posting;
    }

    private boolean additionalLinesRequired(PaymentTargetBO target) {
        return !target.isAllCurrenciesMatch() && ledgerAccountId(target.getCreditorAccount()).isPresent();
    }

    // Here oprId is postingId for debitLine and paymentId for creditLine
    private PostingLineBO createLine(LedgerBO ledger, PaymentTargetBO target, LocalDateTime pstTime, List<ExchangeRateBO> rates, String oprId, boolean isDebitLine, boolean isFirstLine) {
        String id = Ids.id();
        ExchangeRateBO ratesForLine = resolveRateIfRequired(getReferenceByValue(target, isFirstLine), rates);
        String targetDetails = serializeService.serializeOprDetails(paymentMapper.toPaymentTargetDetails(id, target, pstTime.toLocalDate(), Optional.ofNullable(ratesForLine)
                                                                                                                                                    .map(Collections::singletonList)
                                                                                                                                                    .orElse(null)));

        LedgerAccountBO ledgerAccount = getLedgerAccount(ledger, target.getPaymentProduct(), getReferenceByValue(target, isDebitLine), isDebitLine, isFirstLine, target.isAllCurrenciesMatch());
        BigDecimal debitAmount = getDCtAmount(target.getInstructedAmount(), isDebitLine, ratesForLine);
        BigDecimal creditAmount = getDCtAmount(target.getInstructedAmount(), !isDebitLine, ratesForLine);
        return postingMapper.buildPostingLine(targetDetails, ledgerAccount, debitAmount, creditAmount, oprId, id);
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
                       ? applyRate(amount.getAmount(), rate)
                       : BigDecimal.ZERO;
    }

    /**
     * example:
     * Debtor cur:   GBP
     * Payment cur:  USD
     * Creditor cur: JPY
     * <p>
     * Rates : 1) from: USD->EUR; to: EUR->GBP
     * 2) from: USD->EUR; to: EUR->JPY
     * <p>
     * lines amount = amount / rateFrom * rateTo (USD->EUR->GBP) (USD->EUR->JPY)
     */
    private BigDecimal applyRate(BigDecimal amount, ExchangeRateBO rate) {
        return Optional.ofNullable(rate)
                       .map(r -> amount.divide(parseBD(r.getRateFrom()), 4, RoundingMode.HALF_EVEN).multiply(parseBD(r.getRateTo())))
                       .orElse(amount);
    }

    private BigDecimal parseBD(String value) {
        return NumberUtils.createBigDecimal(value);
    }

    private LedgerAccountBO getLedgerAccount(LedgerBO ledger, PaymentProductBO paymentProduct, AccountReferenceBO reference, boolean isDebitLine, boolean isFirstLine, boolean isAllCurrenciesMatch) {
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
