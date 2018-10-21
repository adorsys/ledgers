package de.adorsys.ledgers.deposit.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.adorsys.ledgers.deposit.domain.BulkPaymentBO;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.domain.PaymentEntity;
import de.adorsys.ledgers.deposit.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.domain.PaymentTarget;
import de.adorsys.ledgers.deposit.domain.SinglePaymentBO;
import de.adorsys.ledgers.deposit.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.service.DepositAccountService;
import de.adorsys.ledgers.postings.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.domain.LedgerBO;
import de.adorsys.ledgers.postings.domain.PostingBO;
import de.adorsys.ledgers.postings.domain.PostingLineBO;
import de.adorsys.ledgers.postings.domain.PostingStatusBO;
import de.adorsys.ledgers.postings.domain.PostingTypeBO;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.postings.service.PostingService;
import de.adorsys.ledgers.util.CloneUtils;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.SerializationUtils;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DepositAccountServiceImpl implements DepositAccountService {
    private final DepositAccountRepository depositAccountRepository;
    private final LedgerService ledgerService;
    private final PostingService postingService;
    private final DepositAccountConfigService depositAccountConfigService;

	@Override
	public DepositAccountBO createDepositAccount(DepositAccountBO depositAccountBO) throws PaymentProcessingException {
		DepositAccount depositAccount = CloneUtils.cloneObject(depositAccountBO, DepositAccount.class);
        LedgerAccountBO depositParentAccount = depositAccountConfigService.getDepositParentAccount();

        // Business logic

        LedgerAccountBO ledgerAccount = new LedgerAccountBO();
        ledgerAccount.setParent(depositParentAccount);
        ledgerAccount.setName(depositAccount.getIban());


        try {
			ledgerService.newLedgerAccount(ledgerAccount);
		} catch (LedgerAccountNotFoundException | LedgerNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
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
        return CloneUtils.cloneObject(saved, DepositAccountBO.class);
    }

	@Override
	public PaymentResultBO<SinglePaymentBO> executeSinglePaymentWithoutSca(SinglePaymentBO paymentBO) throws PaymentProcessingException {
		PaymentEntity payment = CloneUtils.cloneObject(paymentBO, PaymentEntity.class);
		
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
		} catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage());
		}
        return null;
    }

	@Override
	public PaymentResultBO<List<SinglePaymentBO> > executeSinglePaymentsWithoutSca(List<SinglePaymentBO> paymentBOList) throws PaymentProcessingException {
		
		for (SinglePaymentBO singlePaymentBO : paymentBOList) {
			executeSinglePaymentWithoutSca(singlePaymentBO);
		}
		
		PaymentResultBO result = null;// TODO
		return result;
	}

	@Override
	public PaymentResultBO<BulkPaymentBO> executeBulkPaymentWithoutSca(BulkPaymentBO paymentBO) throws PaymentProcessingException {
		PaymentEntity payment = CloneUtils.cloneObject(paymentBO, PaymentEntity.class);
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

//        todo: how we should proceed with batchBookingPreferred = true ?

        List<PostingLineBO> lines = new ArrayList<>();

        for (PaymentTarget singlePayment : payment.getTargets()) {

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

        PostingBO posting = buildPosting(oprDetails, ledger, lines);
        try {
			postingService.newPosting(posting);
		} catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new PaymentProcessingException(e.getMessage(), e);
		}

        return null;
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
    private LedgerAccountBO getDebtorLedgerAccount(LedgerBO ledger, PaymentEntity payment) throws PaymentProcessingException, LedgerNotFoundException {
        String iban = payment.getDebtorAccount().getIban();
//        DepositAccount debtorDepositAccount = depositAccountRepository.findByIban(iban).orElseThrow(() -> new NotFoundException("TODO Map some error"));
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
