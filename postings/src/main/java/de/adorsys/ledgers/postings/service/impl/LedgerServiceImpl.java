package de.adorsys.ledgers.postings.service.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.basetypes.LedgerAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerName;
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
	
	private NamePatterns namePaterns;

	/**
	 * Creates a new ledger.
	 */
	@Override
	public Ledger newLedger(Ledger ledger) {
		// We will have to crate valid accounts if they do not exist.
		Optional<ChartOfAccount> coaOptions = chartOfAccountRepo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(ledger.getCoa(), ledger.getValidFrom(), ledger.getValidFrom());
		if(!coaOptions.isPresent()){
			throw new IllegalArgumentException(String.format("Nor valid chart of account with name %s found at date %s", ledger.getCoa(), ledger.getValidFrom()));
		}
		ChartOfAccount chartOfAccount = coaOptions.get();

		// Existence of the a ledger with this name.
		Optional<Ledger> existingOption = ledgerRepository.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(ledger.getName(), ledger.getValidFrom(), ledger.getValidFrom());
		
		if(existingOption.isPresent()){
			// The we will have to set the valid to.
			Ledger existing = existingOption.get();
			existing.setValidTo(ledger.getValidFrom());
			ledgerRepository.save(existing);
		}
		
		Ledger newLedger = Ledger.builder()
			.coa(ledger.getCoa())
			.created(LocalDateTime.now())
			.id(Ids.id())
			.name(ledger.getName())
			.user(principal.getName())
			.validFrom(ledger.getValidFrom())
			.build();
		newLedger = ledgerRepository.save(newLedger);
		
		// Get all root ledger account types of corresponding chart of account.
		List<LedgerAccountType> ledgerAccountTypes = ledgerAccountTypeRepo.findByCoaAndLevelAndValidFromBeforeAndValidToAfter(ledger.getCoa(), 0, ledger.getValidFrom(), ledger.getValidFrom());
		if(ledgerAccountTypes.isEmpty()){
			throw new IllegalStateException(String.format("Missing ledger account types for chart of account with name %s at date %s", ledger.getCoa(), ledger.getValidFrom()));
		}
			
		// Create corresponding ledger account if not yet done so.
		ledgerAccountTypes.forEach(lat ->{
			// Look for a chart of account with ledger name and ledger account type name
			List<LedgerAccount> ledgerAccounts = ledgerAccountRepository.findByLedgerAndLevelAndAccountTypeAndValidFromBeforeAndValidToAfter(ledger.getName(), lat.getLevel(), lat.getName(), ledger.getValidFrom(), ledger.getValidFrom());
			if(ledgerAccounts.isEmpty()){
				LedgerAccountName ledgerAccountName = namePaterns.toAccountName(chartOfAccount.toName(), lat.toName(), ledger.toName());
				// Then create one.
				LedgerAccount.builder()
					.id(Ids.id())
					.ledger(ledger.getName())
					.level(0)
					.name(ledgerAccountName.getValue())// Produce the name from the type, coa,
					.parent(ledgerAccountName.getValue()) // For Root parent same as child
					.accountType(lat.getName())
					.user(principal.getName())
					.build();
			}
		});
		return CloneUtils.cloneObject(newLedger, Ledger.class);
	}

	@Override
	public List<Ledger> findLedgersByName(LedgerName name) {
		return CloneUtils.cloneList(ledgerRepository.findByName(name.getValue()), Ledger.class);
	}

	@Override
	public Optional<Ledger> findLedgersByName(LedgerName name, LocalDateTime referenceDate) {
		Ledger ledger = ledgerRepository.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name.getValue(), referenceDate, referenceDate).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(ledger, Ledger.class));
	}

	@Override
	public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount) {
		// The valid from is the reference date.
		LocalDateTime refDate = ledgerAccount.getValidFrom();
		
		// Handle existing valid ledger account with same name.
		Optional<LedgerAccount> existingOptions = ledgerAccountRepository
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
						ledgerAccount.getName(),
						refDate, refDate);
		if (existingOptions.isPresent()) {
			// Check if same parent. We do not allow changing tree structure.
			if (!existingOptions.get().getParent().equals(ledgerAccount.getParent())) {
				throw new IllegalArgumentException(String.format("Ledger account with name %s has parent of name %s. ",
						ledgerAccount.getName(), ledgerAccount.getParent()));
			}

			// Set valid to
			LedgerAccount la = existingOptions.get();
			la.setValidTo(refDate);
			ledgerAccountRepository.save(la);
		}

		// Find valid parent at ref date
		Optional<LedgerAccount> parentOptions = ledgerAccountRepository
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
				ledgerAccount.getParent(), refDate, refDate);
		if (!parentOptions.isPresent())
			throw new IllegalStateException(
					String.format("Missing corrsponding parent with name %s and valid at date %s", ledgerAccount.getParent(), refDate));
		
		LedgerAccount parentAccount = parentOptions.get();
		
		// Load the ledger account type of ledger account
		Optional<LedgerAccountType> accountTypeOptions = ledgerAccountTypeRepo.
			findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
					ledgerAccount.getAccountType(), refDate, refDate);
		if(!accountTypeOptions.isPresent())
			throw new IllegalArgumentException(
					String.format("Missing account type of ledger account with type name %s and valid at date %s", ledgerAccount.getAccountType(), refDate));
		
		LedgerAccountType ledgerAccountType = accountTypeOptions.get();

		Optional<LedgerAccountType> parentTypeOptions = ledgerAccountTypeRepo
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
						parentAccount.getAccountType(), refDate, refDate);
		if(!parentTypeOptions.isPresent())
			throw new IllegalStateException(
					String.format("Missing account type of parent account with type name %s and valid at date %s", parentAccount.getAccountType(), refDate));
		
		LedgerAccountType parentAccountType = parentTypeOptions.get();
		
		// Any grand child type might be use as type of ledger account. 
		LedgerAccountType progenyType = ledgerAccountType;
		
		while(!progenyType.getId().equals(parentAccountType.getId()) && // either account and parent share the same type 
				!progenyType.getParent().equals(parentAccountType.getName()) && // of parent of account type is type of parent account
				progenyType.getLevel()>parentAccountType.getLevel()){ // While child type still on higher level
			progenyType = ledgerAccountTypeRepo.
					findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
							progenyType.getParent(), refDate, refDate).orElse(null);
			
			// progeny is not a sub account of the parent account type. Strike.
			if(progenyType==null || progenyType.getLevel()==parentAccountType.getLevel())
				throw new IllegalArgumentException(
						String.format("Parent account type with name %s not an ancestor of provided ledger account type with name %s at date %s", 
								parentAccount.getAccountType(), ledgerAccount.getAccountType(), refDate));
		}
		
		LedgerAccount newLedgerAccount = LedgerAccount.builder()
			.accountType(ledgerAccount.getAccountType())
			.created(LocalDateTime.now())
			.id(Ids.id())
			.ledger(parentAccount.getLedger())
			.level(parentAccount.getLevel()+1)
			.name(ledgerAccount.getName())
			.parent(parentAccount.getName())
			.user(principal.getName())
			.validFrom(refDate)
			.build();
		newLedgerAccount = ledgerAccountRepository.save(newLedgerAccount);
		
		return CloneUtils.cloneObject(newLedgerAccount, LedgerAccount.class);
	}

	@Override
	public Optional<LedgerAccount> findLedgerAccount(LedgerAccountName name, LocalDateTime referenceDate) {
		LedgerAccount ledgerAccount = ledgerAccountRepository.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(
				name.getValue(), referenceDate, referenceDate).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(ledgerAccount, LedgerAccount.class));
	}

	@Override
	public List<LedgerAccount> findLedgerAccounts(LedgerAccountName name) {
		return CloneUtils.cloneList(ledgerAccountRepository.findByName(name.getValue()), LedgerAccount.class);
	}
	
}
