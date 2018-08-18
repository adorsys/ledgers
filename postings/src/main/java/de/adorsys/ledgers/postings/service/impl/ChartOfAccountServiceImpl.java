package de.adorsys.ledgers.postings.service.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.utils.CloneUtils;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.postings.utils.NamePatterns;

@Service
@Transactional
public class ChartOfAccountServiceImpl implements ChartOfAccountService {
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepo;

	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepo;

	@Autowired
	private Principal principal;
	
	@Autowired
	private NamePatterns np;

	/**
	 * Create a new chart of account. 
	 * 	- Generate a new id, 
	 * 	- Sets the creation time, 
	 * 	- Set the creating user from user principal.
	 * 
	 * If there is a pre-existing chart of account with the same name, set the
	 * valid to of that one to be the valid from of the new one.
	 * 
	 * Create root accounts if they do no exist.
	 * 
	 */
	@Override
	public ChartOfAccount newChartOfAccount(ChartOfAccount coa,
			List<String> rootAccountTypes) {

		if (rootAccountTypes == null || rootAccountTypes.isEmpty()) {
				throw new IllegalArgumentException("At least one root account types must be specified.");
		}
		
		LocalDateTime created = LocalDateTime.now();
		String user = principal.getName();

		// Save new coa
		coa = new ChartOfAccount(Ids.id(), coa.getName(), created, user, coa.getDesc());
		final ChartOfAccount coa2 = chartOfAccountRepo.save(coa);

		rootAccountTypes.forEach(rat -> {
			String accountTypeName = np.toAccountTypeName(coa2, rat);
			LedgerAccountType ledgerAccountType = new LedgerAccountType(Ids.id(), accountTypeName, created, user, rat, coa2, accountTypeName, 0);
			ledgerAccountTypeRepo.save(ledgerAccountType);
		});

		return CloneUtils.cloneObject(coa2, ChartOfAccount.class);
	}
	
	@Override
	public Optional<ChartOfAccount> findChartOfAccountsById(String id) {
		return chartOfAccountRepo.findById(id);
	}

	@Override
	public Optional<ChartOfAccount> findChartOfAccountsByName(String name) {
		ChartOfAccount coa = chartOfAccountRepo.findOptionalByName(name).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(coa, ChartOfAccount.class));
	}

	/**
	 * First check is ledger account with name is in database. If yes, set valid
	 * to.
	 */
	@Override
	public LedgerAccountType newLedgerAccountType(LedgerAccountType parent, String name, String desc) {
		LocalDateTime created = LocalDateTime.now();
		String user = principal.getName();
		String parentId = parent.getId();
		parent = ledgerAccountTypeRepo.findById(parentId).orElseThrow(() -> new IllegalStateException(String.format("Missing ledger account type with id %s", parentId)));
		LedgerAccountType ledgerAccountType = new LedgerAccountType(Ids.id(), name, created, user, desc, parent.getCoa(), parent.getName(), parent.getLevel()+1);
		LedgerAccountType saved = ledgerAccountTypeRepo.save(ledgerAccountType);
		return CloneUtils.cloneObject(saved, LedgerAccountType.class);
	}

	@Override
	public Optional<LedgerAccountType> findLedgerAccountTypeById(String id) {
		return ledgerAccountTypeRepo.findById(id);
	}

	@Override
	public Optional<LedgerAccountType> findLedgerAccountType(String name) {
		LedgerAccountType lat = ledgerAccountTypeRepo.findOptionalByName(name).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(lat, LedgerAccountType.class));
	}

	@Override
	public List<LedgerAccountType> findChildLedgerAccountTypes(String parentName) {
		List<LedgerAccountType> found = ledgerAccountTypeRepo.findByParentOrderByLevelDesc(parentName);
		return CloneUtils.cloneList(found, LedgerAccountType.class);
	}

	@Override
	public List<LedgerAccountType> findCoaLedgerAccountTypes(String coaName) {
		ChartOfAccount coa = chartOfAccountRepo.findOptionalByName(coaName).orElseThrow(()->new IllegalStateException(String.format("Chart of account with name %s not found", coaName)));
		List<LedgerAccountType> found = ledgerAccountTypeRepo.findByCoaOrderByLevelDesc(coa);
		return CloneUtils.cloneList(found, LedgerAccountType.class);
	}

	@Override
	public List<LedgerAccountType> findCoaRootAccountTypes(String coaName) {
		ChartOfAccount coa = chartOfAccountRepo.findOptionalByName(coaName).orElseThrow(()->new IllegalStateException(String.format("Chart of account with name %s not found", coaName)));
		List<LedgerAccountType> found = ledgerAccountTypeRepo.findByCoaAndLevel(coa, 0);
		return CloneUtils.cloneList(found, LedgerAccountType.class);
	}
}
