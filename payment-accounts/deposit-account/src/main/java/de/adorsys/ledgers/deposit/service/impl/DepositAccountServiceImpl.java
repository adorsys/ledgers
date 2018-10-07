package de.adorsys.ledgers.deposit.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.service.DepositAccountService;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.utils.CloneUtils;
import de.adorsys.ledgers.utils.Ids;

@Service
public class DepositAccountServiceImpl implements DepositAccountService {

	@Autowired
	private DepositAccountRepository depositAccountRepository;
	
	@Autowired
	private LedgerService ledgerService;
	
	@Override
	public DepositAccount createDepositAccount(DepositAccount depositAccount, String ledgerName, String depositParentAccountNumber) throws NotFoundException{
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
			.parent(LedgerAccount.builder().name(depositParentAccountNumber).ledger(Ledger.builder().name(ledgerName).build()).build())
			.name(depositAccount.getIban())
			.build();
		ledgerService.newLedgerAccount(ledgerAccount);
		
		DepositAccount saved = depositAccountRepository.save(da);
		return CloneUtils.cloneObject(saved, DepositAccount.class);
		
	}
}
