package de.adorsys.ledgers.deposit.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

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
import de.adorsys.ledgers.utils.CloneUtils;
import de.adorsys.ledgers.utils.Ids;
import de.adorsys.ledgers.utils.SerializationUtils;

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
		
		LedgerAccount ledgerAccount = LedgerAccount.builder()
			.parent(depositParentAccount)
			.name(depositAccount.getIban())
			.build();
		try {
			ledgerService.newLedgerAccount(ledgerAccount);
		} catch (NotFoundException e) {
			throw new IllegalStateException(e);// TODO Deal with this
		}
		
		DepositAccount saved = depositAccountRepository.save(da);
		return CloneUtils.cloneObject(saved, DepositAccount.class);
		
	}
	
    @Override
    public SinglePayment executeSinglePaymentWithoutSca(SinglePayment payment, String ledgerName) throws NotFoundException, JsonProcessingException {
    	LocalDateTime now = LocalDateTime.now();
    	String oprDetails = SerializationUtils.writeValueAsString(payment);
		Ledger ledger = depositAccountConfigService.getLedger();
		
		// Validation debtor account number
		String iban = payment.getDebtorAccount().getIban();
		DepositAccount debtorDepositAccount = depositAccountRepository.findByIban(iban).orElseThrow(() -> new NotFoundException("TODO Map some error"));
		LedgerAccount debtorLedgerAccount = ledgerService.findLedgerAccount(ledger, iban).orElseThrow(() -> new NotFoundException("TODO Map some error"));
		
		String creditorIban = payment.getCreditorAccount().getIban();
		LedgerAccount creditLedgerAccount = ledgerService.findLedgerAccount(ledger, creditorIban).orElseGet(() -> {
			return depositAccountConfigService.getClearingAccount();
		});
				
		
		PostingLine debitLine = PostingLine.builder()
			.details(oprDetails)
			.account(debtorLedgerAccount)
			.debitAmount(payment.getInstructedAmount().getAmount())
			.creditAmount(BigDecimal.ZERO)
			.build();

		PostingLine creditLine = PostingLine.builder()
				.details(oprDetails)
				.account(creditLedgerAccount)
				.debitAmount(BigDecimal.ZERO)
				.creditAmount(payment.getInstructedAmount().getAmount())
				.build();
		List<PostingLine> lines = Arrays.asList(debitLine, creditLine);
		
		Posting posting = Posting.builder()
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

		Posting newPosting = postingService.newPosting(posting);
        return null;
    }
}
