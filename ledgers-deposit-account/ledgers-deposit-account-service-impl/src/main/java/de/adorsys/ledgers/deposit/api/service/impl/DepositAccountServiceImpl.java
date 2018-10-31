package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.ledgers.deposit.api.domain.BulkPaymentBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.SinglePaymentBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentTarget;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DepositAccountServiceImpl implements DepositAccountService {

    private DepositAccountRepository depositAccountRepository;
    private LedgerService ledgerService;
    private PostingService postingService;
    private DepositAccountConfigService depositAccountConfigService;
    private DepositAccountMapper depositAccountMapper;

    public DepositAccountServiceImpl(DepositAccountRepository depositAccountRepository, LedgerService ledgerService,
                                     PostingService postingService, DepositAccountConfigService depositAccountConfigService, DepositAccountMapper depositAccountMapper) {
        super();
        this.depositAccountRepository = depositAccountRepository;
        this.ledgerService = ledgerService;
        this.postingService = postingService;
        this.depositAccountConfigService = depositAccountConfigService;
        this.depositAccountMapper = depositAccountMapper;
    }

    @Override
    public DepositAccountBO createDepositAccount(DepositAccountBO depositAccountBO) throws PaymentProcessingException {
        DepositAccount depositAccount = depositAccountMapper.toDepositAccount(depositAccountBO);
        LedgerAccountBO depositParentAccount = depositAccountConfigService.getDepositParentAccount();

        // Business logic
        LedgerAccountBO ledgerAccount = new LedgerAccountBO();
        ledgerAccount.setParent(depositParentAccount);
        ledgerAccount.setName(depositAccount.getIban());


        try {
            ledgerService.newLedgerAccount(ledgerAccount);
        } catch (LedgerAccountNotFoundException | LedgerNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e); //@fpo Is that for sure? CreateDepositAccount should throw PaymentProcessException?
        }

        DepositAccount da = new DepositAccount();
        da.setId(Ids.id());
        da.setAccountStatus(depositAccount.getAccountStatus());
        da.setAccountType(depositAccount.getAccountType());
        da.setCurrency(depositAccount.getCurrency());
        da.setDetails(depositAccount.getDetails());
        da.setIban(depositAccount.getIban());
        da.setLinkedAccounts(depositAccount.getLinkedAccounts());
        da.setMsisdn(depositAccount.getMsisdn());
        da.setName(depositAccount.getName());
        da.setProduct(depositAccount.getProduct());
        da.setUsageType(depositAccount.getUsageType());

        DepositAccount saved = depositAccountRepository.save(da);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    @Override
    public DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException {
        return depositAccountRepository.findById(accountId)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .orElseThrow(() -> new DepositAccountNotFoundException(accountId));
    }

    @Override
    public DepositAccountBO getDepositAccountByIBAN(String iban) throws DepositAccountNotFoundException {
        return depositAccountRepository.findByIban(iban)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .orElseThrow(() -> new DepositAccountNotFoundException(iban));
    }

    @Override
    public PaymentResultBO<SinglePaymentBO> executeSinglePaymentWithoutSca(SinglePaymentBO paymentBO) throws PaymentProcessingException {
        Payment payment = CloneUtils.cloneObject(paymentBO, Payment.class);

        String oprDetails;

        try {
            oprDetails = SerializationUtils.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized", e);
        }
        LedgerBO ledger = depositAccountConfigService.getLedger();

        // Validation debtor account number
        LedgerAccountBO debtorLedgerAccount;
        try {
            debtorLedgerAccount = getDebtorLedgerAccount(ledger, payment);
        } catch (LedgerNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }

        PaymentTarget paymentTarget = payment.getTargets().get(0);
        String creditorIban = paymentTarget.getCreditorAccount().getIban();
        LedgerAccountBO creditLedgerAccount;
        try {
            creditLedgerAccount = ledgerService.findLedgerAccount(ledger, creditorIban).orElseGet(() -> depositAccountConfigService.getClearingAccount());
        } catch (LedgerNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }

        BigDecimal amount = paymentTarget.getInstructedAmount().getAmount();

        PostingLineBO debitLine = buildDebitLine(oprDetails, debtorLedgerAccount, amount);

        PostingLineBO creditLine = buildCreditLine(oprDetails, creditLedgerAccount, amount);

        List<PostingLineBO> lines = Arrays.asList(debitLine, creditLine);

        PostingBO posting = buildPosting(oprDetails, ledger, lines);

        try {
            postingService.newPosting(posting);
        } catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | BaseLineException e) {
            throw new PaymentProcessingException(e.getMessage());
        }
        return null;
    }

    @Override
    public PaymentResultBO<List<SinglePaymentBO>> executeSinglePaymentsWithoutSca(List<SinglePaymentBO> paymentBOList) throws PaymentProcessingException {

        for (SinglePaymentBO singlePaymentBO : paymentBOList) {
            executeSinglePaymentWithoutSca(singlePaymentBO);
        }

//		PaymentResultBO result = null;// TODO by @fpo
        return null;
    }

    @Override
    public PaymentResultBO<BulkPaymentBO> executeBulkPaymentWithoutSca(BulkPaymentBO paymentBO) throws PaymentProcessingException {
        Payment payment = CloneUtils.cloneObject(paymentBO, Payment.class);
        String oprDetails;
        try {
            oprDetails = SerializationUtils.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized");
        }
        LedgerBO ledger = depositAccountConfigService.getLedger();

        // Validation debtor account number
        LedgerAccountBO debtorLedgerAccount;
        try {
            debtorLedgerAccount = getDebtorLedgerAccount(ledger, payment);
        } catch (LedgerNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }

//        todo: how we should proceed with batchBookingPreferred = true ?  by @speex

        List<PostingLineBO> lines = new ArrayList<>();

        for (PaymentTarget singlePayment : payment.getTargets()) {
            processSingleTarget(oprDetails, ledger, debtorLedgerAccount, lines, singlePayment);
        }

        PostingBO posting = buildPosting(oprDetails, ledger, lines);
        try {
            postingService.newPosting(posting);
        } catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | BaseLineException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }

        return null;
    }

    private void processSingleTarget(String oprDetails, LedgerBO ledger, LedgerAccountBO debtorLedgerAccount,
                                     final List<PostingLineBO> lines, PaymentTarget singlePayment) throws PaymentProcessingException {
        String creditorIban = singlePayment.getCreditorAccount().getIban();

        LedgerAccountBO creditLedgerAccount;
        try {
            creditLedgerAccount = ledgerService.findLedgerAccount(ledger, creditorIban).orElseGet(() -> depositAccountConfigService.getClearingAccount());
        } catch (LedgerNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
        }

        PostingLineBO debitLine = buildDebitLine(oprDetails, debtorLedgerAccount, singlePayment.getInstructedAmount().getAmount());
        lines.add(debitLine);

        PostingLineBO creditLine = buildCreditLine(oprDetails, creditLedgerAccount, singlePayment.getInstructedAmount().getAmount());
        lines.add(creditLine);
    }

    private PostingLineBO buildCreditLine(String oprDetails, LedgerAccountBO creditLedgerAccount, BigDecimal amount) {
        return buildPostingLine(oprDetails, creditLedgerAccount, BigDecimal.ZERO, amount);
    }

    private PostingLineBO buildDebitLine(String oprDetails, LedgerAccountBO debtorLedgerAccount, BigDecimal amount) {
        return buildPostingLine(oprDetails, debtorLedgerAccount, amount, BigDecimal.ZERO);
    }

    private PostingLineBO buildPostingLine(String oprDetails, LedgerAccountBO creditLedgerAccount, BigDecimal debitAmount, BigDecimal creditAmount) {
        PostingLineBO p = new PostingLineBO();
        p.setDetails(oprDetails);
        p.setAccount(creditLedgerAccount);
        p.setDebitAmount(debitAmount);
        p.setCreditAmount(creditAmount);
        return p;
    }

    @NotNull
    private LedgerAccountBO getDebtorLedgerAccount(LedgerBO ledger, Payment payment) throws PaymentProcessingException, LedgerNotFoundException {
        String iban = payment.getDebtorAccount().getIban();
        return ledgerService.findLedgerAccount(ledger, iban).orElseThrow(() -> new PaymentProcessingException("Ledger account was not found by iban=" + iban));
    }

    private PostingBO buildPosting(String oprDetails, LedgerBO ledger, List<PostingLineBO> lines) {
        LocalDateTime now = LocalDateTime.now();
        PostingBO p = new PostingBO();
        p.setOprId(Ids.id());
        p.setOprTime(now);
        p.setOprDetails(oprDetails);
        p.setPstTime(now);
        p.setPstType(PostingTypeBO.BUSI_TX);
        p.setPstStatus(PostingStatusBO.POSTED);
        p.setLedger(ledger);
        p.setValTime(now);
        p.setLines(lines);
        return p;
    }

}
