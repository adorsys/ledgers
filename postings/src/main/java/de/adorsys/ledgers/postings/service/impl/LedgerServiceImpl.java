package de.adorsys.ledgers.postings.service.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.postings.utils.CloneUtils;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.postings.utils.NamePatterns;

@Service
@Transactional
public class LedgerServiceImpl implements LedgerService {
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepo;
	
	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepo;
	
	@Autowired
	private Principal principal;
	
	@Autowired
	private LedgerRepository ledgerRepository;
	
	@Autowired
	private LedgerAccountRepository ledgerAccountRepository;
	
	@Autowired
	private NamePatterns namePaterns;

	/**
	 * Creates a new ledger.
	 */
	@Override
	public Ledger newLedger(Ledger ledger) {
		ChartOfAccount coa = chartOfAccountRepo.findById(ledger.getCoa().getId()).orElseThrow(() -> new IllegalArgumentException(String.format("Chart of account with id %s not found.", ledger.getCoa().getId())));
		LocalDateTime created = LocalDateTime.now();
		String user = principal.getName();
		Ledger newLedger = new Ledger(Ids.id(), ledger.getName(), created, user, ledger.getDesc(), coa);
		Ledger savedLedger = ledgerRepository.save(newLedger);
		
		// Get all root ledger account types of corresponding chart of account.
		List<LedgerAccountType> ledgerAccountTypes = ledgerAccountTypeRepo.findByCoaAndLevel(coa, 0);
		if(ledgerAccountTypes.isEmpty()){
			throw new IllegalStateException(String.format("Missing ledger account types for chart of account with name %s", ledger.getCoa()));
		}
			
		// Create corresponding ledger account if not yet done so.
		ledgerAccountTypes.forEach(lat ->{
			// Look for a chart of account with ledger name and ledger account type name
			String ledgerAccountName = namePaterns.toAccountName(savedLedger, lat, null);
			new LedgerAccount(Ids.id(), ledgerAccountName, created, user, ledgerAccountName, created, savedLedger, ledgerAccountName, lat, 0);
		});
		return CloneUtils.cloneObject(savedLedger, Ledger.class);
	}

	@Override
	public Optional<Ledger> findLedgerByName(String name) {
		return Optional.ofNullable(CloneUtils.cloneObject(ledgerRepository.findOptionalByName(name).orElse(null), Ledger.class));
	}

	@Override
	public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount) {
		// The valid from is the reference date.
		LocalDateTime refDate = ledgerAccount.getValidFrom();
		
		// Handle existing valid ledger account with same name.
		LedgerAccount parentAccount = ledgerAccountRepository
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
						ledgerAccount.getParent(),refDate, refDate)
				.orElseThrow(()-> new IllegalArgumentException(String.format("Missing corrsponding parent with name %s and valid at date %s",
						ledgerAccount.getParent(), refDate)));

		// Load the ledger account type of ledger account
		LedgerAccountType ledgerAccountType = ledgerAccountTypeRepo.findById(ledgerAccount.getAccountType().getId())
			.orElseThrow(()->new IllegalArgumentException(
					String.format("Missing account type of ledger account with type name %s and valid at date %s", ledgerAccount.getAccountType(), refDate)));

		LedgerAccountType parentAccountType = ledgerAccountTypeRepo.findById(parentAccount.getAccountType().getId())
				.orElseThrow(()->new IllegalArgumentException(
						String.format("Missing account type of parent account with type name %s and valid at date %s", parentAccount.getAccountType(), refDate)));
		
		// Any grand child type might be use as type of ledger account. 
		LedgerAccountType progenyType = ledgerAccountType;
		
		while(!progenyType.getId().equals(parentAccountType.getId()) && // either account and parent share the same type 
				!progenyType.getParent().equals(parentAccountType.getName()) && // or parent of account type is type of parent account
				progenyType.getLevel()>parentAccountType.getLevel()){ // or child type still on higher level
			progenyType = ledgerAccountTypeRepo.findOptionalByName(progenyType.getParent())
					.orElseThrow(()->new IllegalArgumentException(
							String.format("Missing account type of parent account with type name %s and valid at date %s", parentAccount.getAccountType(), refDate)));
		}

		// progeny is not a sub account of the parent account type. Strike.
		if(progenyType.getLevel()>=parentAccountType.getLevel())
			throw new IllegalArgumentException(
					String.format("Parent account type with name %s not an ancestor of provided ledger account type with name %s at date %s", 
							parentAccount.getAccountType(), ledgerAccount.getAccountType().getName(), refDate));
		
		LedgerAccount newLedgerAccount = new LedgerAccount(Ids.id(), ledgerAccount.getName(), LocalDateTime.now(), 
				principal.getName(), ledgerAccount.getDesc(), ledgerAccount.getValidFrom(), 
				parentAccount.getLedger(), parentAccount.getName(), ledgerAccountType, parentAccount.getLevel()+1);

		newLedgerAccount = ledgerAccountRepository.save(newLedgerAccount);
		
		return CloneUtils.cloneObject(newLedgerAccount, LedgerAccount.class);
	}

	@Override
	public Optional<LedgerAccount> findLedgerAccount(String name, LocalDateTime referenceDate) {
		LedgerAccount ledgerAccount = ledgerAccountRepository.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
				name, referenceDate, referenceDate).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(ledgerAccount, LedgerAccount.class));
	}

	@Override
	public List<LedgerAccount> findLedgerAccounts(String name) {
		return CloneUtils.cloneList(ledgerAccountRepository.findByName(name), LedgerAccount.class);
	}
	
}
