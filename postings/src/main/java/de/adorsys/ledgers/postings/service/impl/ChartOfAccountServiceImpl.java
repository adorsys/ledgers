package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.utils.CloneUtils;
import de.adorsys.ledgers.postings.utils.Ids;

@Service
@Transactional
public class ChartOfAccountServiceImpl extends AbstractServiceImpl implements ChartOfAccountService {

	/**
	 * Create a new chart of account. 
	 * 
	 * Generate a new id
	 * Sets the creation time
	 * Set the creating user from user principal.
	 * 
	 */
	@Override
	public ChartOfAccount newChartOfAccount(ChartOfAccount coa) {
		LocalDateTime created = LocalDateTime.now();
		String user = principal.getName();
		// Save new coa
		coa = new ChartOfAccount(Ids.id(), created, user, coa.getShortDesc(), coa.getLongDesc(), coa.getName());

		// Return clone.
		return CloneUtils.cloneObject(chartOfAccountRepo.save(coa), ChartOfAccount.class);
	}

	@Override
	public Optional<ChartOfAccount> findChartOfAccountsById(String id) {
		ChartOfAccount coa = chartOfAccountRepo.findById(id).orElse(null);
		// save and return clone.
		return Optional.ofNullable( CloneUtils.cloneObject(chartOfAccountRepo.save(coa), ChartOfAccount.class));
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
	public LedgerAccountType newLedgerAccountType(LedgerAccountType model) {

		// Load persistent instance of coa
		ChartOfAccount coa = loadCoa(model.getCoa());
		final String coaName = coa.getName();

		// Load persistent instance of parent if specified.
		// If not specified, we will assume caller is creating a root account type
		LedgerAccountType parent = null;
		if (model.getParent() != null)
			parent = ledgerAccountTypeRepo.findOptionalByCoaAndName(coa, model.getParent())
					.orElseThrow(() -> new IllegalStateException(String.format(
							"Missing ledger account type with chart of account name %s and account type name %s",
							coaName, model.getParent())));
		String parentName = parent != null ? parent.getName() : namePatterns.toAccountTypeName(coa, "NULL");

		LocalDateTime created = LocalDateTime.now();
		String user = principal.getName();

		// Validate presence of name, must be unique in the scope of this chart of account.
		if (model.getName() == null)
			throw new IllegalArgumentException(String.format("Missing model name."));

		// Validate account side D or C
		if (model.getBalanceSide() == null)
			throw new IllegalArgumentException(String.format("Missing model field increasesTo."));

		// Check level Set it to 0 if root account type
		int level = parent != null ? parent.getLevel() + 1 : 0;

		// Creae ledger account type object
		LedgerAccountType ledgerAccountType = new LedgerAccountType(Ids.id(), created, user, model.getShortDesc(),
				model.getLongDesc(), model.getName(), coa, parentName, level, model.getBalanceSide());
		
		// persist and return clone.
		return CloneUtils.cloneObject(ledgerAccountTypeRepo.save(ledgerAccountType), LedgerAccountType.class);
	}

	@Override
	public Optional<LedgerAccountType> findLedgerAccountTypeById(String id) {
		LedgerAccountType lat = ledgerAccountTypeRepo.findById(id).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(lat, LedgerAccountType.class));
	}

	@Override
	public Optional<LedgerAccountType> findLedgerAccountType(ChartOfAccount chartOfAccount, String name) {
		ChartOfAccount coa = loadCoa(chartOfAccount);
		LedgerAccountType lat = ledgerAccountTypeRepo.findOptionalByCoaAndName(coa, name).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(lat, LedgerAccountType.class));
	}

	@Override
	public List<LedgerAccountType> findChildLedgerAccountTypes(ChartOfAccount chartOfAccount, String parentName) {
		ChartOfAccount coa = loadCoa(chartOfAccount);
		List<LedgerAccountType> lats = ledgerAccountTypeRepo.findByCoaAndParent(coa, parentName);
		return CloneUtils.cloneList(lats, LedgerAccountType.class);
	}

	@Override
	public List<LedgerAccountType> findCoaLedgerAccountTypes(ChartOfAccount chartOfAccount) {
		ChartOfAccount coa = loadCoa(chartOfAccount);
		List<LedgerAccountType> lats = ledgerAccountTypeRepo.findByCoaOrderByLevelDesc(coa);
		return CloneUtils.cloneList(lats, LedgerAccountType.class);
	}

	@Override
	public List<LedgerAccountType> findCoaAccountTypesByLevel(ChartOfAccount chartOfAccount, int level) {
		ChartOfAccount coa = loadCoa(chartOfAccount);
		List<LedgerAccountType> lats = ledgerAccountTypeRepo.findByCoaAndLevel(coa, level);
		return CloneUtils.cloneList(lats, LedgerAccountType.class);
	}
}
