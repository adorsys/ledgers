package de.adorsys.ledgers.deposit.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.adorsys.ledgers.deposit.domain.BasePayment;
import de.adorsys.ledgers.deposit.domain.BulkPayment;
import de.adorsys.ledgers.deposit.exception.PaymentProcessingException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.adorsys.ledgers.deposit.domain.SinglePayment;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.service.DepositAccountService;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingLine;
import de.adorsys.ledgers.postings.domain.PostingStatus;
import de.adorsys.ledgers.postings.domain.PostingType;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.postings.service.PostingService;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.SerializationUtils;

@Service
public class DepositAccountServiceImpl implements DepositAccountService {

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private PostingService postingService;

    @Autowired
    private DepositAccountConfigService depositAccountConfigService;

    @Override
    public DepositAccount createDepositAccount(DepositAccount depositAccount) {
        LedgerAccount depositParentAccount = depositAccountConfigService.getDepositParentAccount();

        // Business logic

        LedgerAccount ledgerAccount = LedgerAccount.builder()
                                              .parent(depositParentAccount)
                                              .name(depositAccount.getIban())
                                              .build();

        try {
            ledgerService.newLedgerAccount(ledgerAccount);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);// TODO Deal with this
        }

        DepositAccount da = DepositAccount.builder()
                                    .id(Ids.id())
                                    .accountStatus(depositAccount.getAccountStatus())
                                    .accountType(depositAccount.getAccountType())
                                    .currency(depositAccount.getCurrency())
                                    .details(depositAccount.getDetails())
                                    .iban(depositAccount.getIban())
                                    .linkedAccounts(depositAccount.getLinkedAccounts())
                                    .msisdn(depositAccount.getMsisdn())
                                    .name(depositAccount.getName())
                                    .product(depositAccount.getProduct())
                                    .usageType(depositAccount.getUsageType())
                                    .build();

        DepositAccount saved = depositAccountRepository.save(da);
        return CloneUtils.cloneObject(saved, DepositAccount.class);
    }

    @Override
    public SinglePayment executeSinglePayment(SinglePayment payment, String ledgerName) throws PaymentProcessingException {

        String oprDetails;

        try {
            oprDetails = SerializationUtils.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized");
        }
        Ledger ledger = depositAccountConfigService.getLedger();

        // Validation debtor account number
        LedgerAccount debtorLedgerAccount = getDebtorLedgerAccount(ledger, payment);

        String creditorIban = payment.getCreditorAccount().getIban();
        LedgerAccount creditLedgerAccount = ledgerService.findLedgerAccount(ledger, creditorIban).orElseGet(() -> depositAccountConfigService.getClearingAccount());

        BigDecimal amount = payment.getInstructedAmount().getAmount();

        PostingLine debitLine = buildDebitLine(oprDetails, debtorLedgerAccount, amount);

        PostingLine creditLine = buildCreditLine(oprDetails, creditLedgerAccount, amount);

        List<PostingLine> lines = Arrays.asList(debitLine, creditLine);

        Posting posting = buildPosting(oprDetails, ledger, lines);

        try {
            postingService.newPosting(posting);
        } catch (NotFoundException e) {
            throw new PaymentProcessingException(e.getMessage());
        }
        return null;
    }

    @Override
    public SinglePayment executeBulkPayment(BulkPayment payment, String ledgerName) throws PaymentProcessingException {

        String oprDetails;
        try {
            oprDetails = SerializationUtils.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized");
        }
        Ledger ledger = depositAccountConfigService.getLedger();

        // Validation debtor account number
        LedgerAccount debtorLedgerAccount = getDebtorLedgerAccount(ledger, payment);

//        todo: how we should proceed with batchBookingPreferred = true ?

        List<PostingLine> lines = new ArrayList<>();

        for (SinglePayment singlePayment : payment.getPayments()) {

            String creditorIban = singlePayment.getCreditorAccount().getIban();

            LedgerAccount creditLedgerAccount = ledgerService.findLedgerAccount(ledger, creditorIban).orElseGet(() -> depositAccountConfigService.getClearingAccount());

            PostingLine debitLine = buildDebitLine(oprDetails, debtorLedgerAccount, singlePayment.getInstructedAmount().getAmount());
            lines.add(debitLine);

            PostingLine creditLine = buildCreditLine(oprDetails, creditLedgerAccount, singlePayment.getInstructedAmount().getAmount());
            lines.add(creditLine);
        }

        Posting posting = buildPosting(oprDetails, ledger, lines);

        try {
            postingService.newPosting(posting);
        } catch (NotFoundException e) {
            throw new PaymentProcessingException(e.getMessage());
        }
        return null;
    }

    private PostingLine buildCreditLine(String oprDetails, LedgerAccount creditLedgerAccount, BigDecimal amount) {
        return buildPostingLine(oprDetails, creditLedgerAccount, BigDecimal.ZERO, amount);
    }

    private PostingLine buildDebitLine(String oprDetails, LedgerAccount debtorLedgerAccount, BigDecimal amount) {
        return buildPostingLine(oprDetails, debtorLedgerAccount, amount, BigDecimal.ZERO);
    }

    private PostingLine buildPostingLine(String oprDetails, LedgerAccount creditLedgerAccount, BigDecimal debitAmount, BigDecimal creditAmount) {
        return PostingLine.builder()
                       .details(oprDetails)
                       .account(creditLedgerAccount)
                       .debitAmount(debitAmount)
                       .creditAmount(creditAmount)
                       .build();
    }

    @NotNull
    private LedgerAccount getDebtorLedgerAccount(Ledger ledger, BasePayment payment) throws PaymentProcessingException {
        String iban = payment.getDebtorAccount().getIban();
//        DepositAccount debtorDepositAccount = depositAccountRepository.findByIban(iban).orElseThrow(() -> new NotFoundException("TODO Map some error"));
        return ledgerService.findLedgerAccount(ledger, iban).orElseThrow(() -> new PaymentProcessingException("Ledger account was not found by iban=" + iban));
    }

    private Posting buildPosting(String oprDetails, Ledger ledger, List<PostingLine> lines) {
        LocalDateTime now = LocalDateTime.now();
        return Posting.builder()
                       .oprId(Ids.id())
                       .oprTime(now)
                       .oprDetails(oprDetails)
                       .pstTime(now)
                       .pstType(PostingType.BUSI_TX)
                       .pstStatus(PostingStatus.POSTED)
                       .ledger(ledger)
                       .valTime(now)
                       .lines(lines)
                       .build();
    }
}
