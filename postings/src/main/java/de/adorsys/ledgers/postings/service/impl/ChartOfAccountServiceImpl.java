package de.adorsys.ledgers.postings.service.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.ledgers.postings.basetypes.ChartOfAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountTypeName;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.utils.Ids;

public class ChartOfAccountServiceImpl implements ChartOfAccountService {
	
	public static final String ACCOUNT_NAME_BALANCE_SHEET_ACCOUNT = "Balance Sheet Accounts";
	public static final String ACCOUNT_NAME_PROFIT_AND_LOST_ACCOUNT = "Profit and Lost Accounts";
	
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepo;
	
	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepo;
	
	@Autowired
	private Principal principal;

	/**
	 * Create a new chart of account. 
	 * 	- Generate a new id,
	 * 	- Sets the creation time,
	 * 	- Set the creating user from user principal.
	 * 
	 */
	@Override
	public ChartOfAccount newChartOfAccount(ChartOfAccount chartOfAccount, List<LedgerAccountTypeName> rootAccountTypes) {
		// Make sure RootAccount Types are not empty
		if(rootAccountTypes==null || rootAccountTypes.isEmpty()) throw new IllegalArgumentException("At least one root account types must be specified.");
		
		ChartOfAccount coa = chartOfAccountRepo.save(ChartOfAccount.newChartOfAccount(chartOfAccount, principal.getName()));
		
		rootAccountTypes.forEach(rat -> {
			// Create the two root ledger account types.
			LedgerAccountType lat = LedgerAccountType.builder()
				.coa(coa.getName())
				.created(coa.getCreated())
				.user(coa.getUser())
				.validFrom(coa.getValidFrom())
				.name(rat.getValue())
				.parent(rat.getValue())
				.level(0)
				.id(Ids.id()).build();
			ledgerAccountTypeRepo.save(lat);
		});
		
		return ChartOfAccount.clone(coa);
	}

	@Override
	public List<ChartOfAccount> findChartOfAccountsByName(ChartOfAccountName name) {
		List<ChartOfAccount> found = chartOfAccountRepo.findByName(name.getValue());
		return ChartOfAccount.clone(found);
	}

	@Override
	public Optional<ChartOfAccount> findChartOfAccountByName(ChartOfAccountName name, LocalDateTime referenceDate) {
		Optional<ChartOfAccount> coaOptions = chartOfAccountRepo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name.getValue(), referenceDate, referenceDate);
		if(!coaOptions.isPresent()) return Optional.empty();
		return Optional.of(ChartOfAccount.clone(coaOptions.get()));
	}

	@Override
	public LedgerAccountType newLedgerAccountType(LedgerAccountType parent, LedgerAccountTypeName name, LocalDateTime validFrom) {
		Optional<LedgerAccountType> parentOptions = ledgerAccountTypeRepo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(parent.getName(), validFrom, validFrom);
		if(!parentOptions.isPresent()) throw new IllegalStateException(String.format("Missing corrsponding parent with name %s and valid at date %s", parent, validFrom));
		LedgerAccountType lat = LedgerAccountType.newChildInstance(name.getValue(), validFrom, principal.getName(), parentOptions.get());
		LedgerAccountType saved = ledgerAccountTypeRepo.save(lat);
		return LedgerAccountType.clone(saved);
	}

	@Override
	public Optional<LedgerAccountType> findLedgerAccountType(LedgerAccountName name, LocalDateTime referenceDate) {
		Optional<LedgerAccountType> latOptions = ledgerAccountTypeRepo.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name.getValue(), referenceDate, referenceDate);
		if(!latOptions.isPresent()) return Optional.empty();
		return Optional.of(LedgerAccountType.clone(latOptions.get()));
	}

	@Override
	public List<LedgerAccountType> findLedgerAccountTypes(LedgerAccountName name) {
		List<LedgerAccountType> found = ledgerAccountTypeRepo.findByName(name.getValue());
		return LedgerAccountType.clone(found);
	}

//	@Override
//	public List<LedgerAccountType> findChildrenLedgerAccountType(LedgerAccountType parent, LocalDateTime referenceDate) {
//		Optional<LedgerAccountType> latOptions = ledgerAccountTypeRepo.findFirstOptionalByNameAndValidFromBeforeOrderByValidFromDesc(name.getValue(), referenceDate);
//		if(!latOptions.isPresent()) return Optional.empty();
//		return Optional.of(LedgerAccountType.clone(latOptions.get()));
//	}
}
