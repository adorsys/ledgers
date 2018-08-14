package de.adorsys.ledgers.postings.service.impl;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.basetypes.ChartOfAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountTypeName;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.utils.Ids;

@Service
@Transactional
public class ChartOfAccountServiceImpl implements ChartOfAccountService {
	@Autowired
	private ChartOfAccountRepository chartOfAccountRepo;

	@Autowired
	private LedgerAccountTypeRepository ledgerAccountTypeRepo;

	@Autowired
	private Principal principal;

	/**
	 * Create a new chart of account. - Generate a new id, - Sets the creation
	 * time, - Set the creating user from user principal.
	 * 
	 * If there is a pre-existing chart of account with the same name, set the
	 * valid to of that one to be the valid from of the new one.
	 * 
	 * Create root accounts if they do no exist.
	 * 
	 */
	@Override
	public ChartOfAccount newChartOfAccount(ChartOfAccount chartOfAccount,
			List<LedgerAccountTypeName> rootAccountTypes) {

		// Check if there is any chart of account valid at the reference time
		// valid from
		Optional<ChartOfAccount> existingCoa = chartOfAccountRepo
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(chartOfAccount.getName(),
						chartOfAccount.getValidFrom(), chartOfAccount.getValidFrom());
		if (existingCoa.isPresent()) {
			// Set the validity to date to the valid from of the new coa
			ChartOfAccount coa = existingCoa.get();
			coa.setValidTo(chartOfAccount.getValidFrom());
			chartOfAccountRepo.save(coa);
		}

		// If no rootAccounts specified, check if some are in the database.
		List<LedgerAccountType> existingRootAccounts = null;
		if (rootAccountTypes == null || rootAccountTypes.isEmpty()) {
			existingRootAccounts = ledgerAccountTypeRepo.findByCoaAndLevelAndValidFromBeforeAndValidToAfter(
					chartOfAccount.getName(), 0, chartOfAccount.getValidFrom(), chartOfAccount.getValidFrom());
			// Throw exception is no root account found for coa.
			if (existingRootAccounts == null || existingRootAccounts.isEmpty()) {
				throw new IllegalArgumentException("At least one root account types must be specified.");
			}
		}

		// Save new coa
		ChartOfAccount coa = chartOfAccountRepo
				.save(ChartOfAccount.newChartOfAccount(chartOfAccount, principal.getName()));

		// Create root account if not yet in database.
		if (rootAccountTypes != null && !rootAccountTypes.isEmpty()) {
			rootAccountTypes.forEach(rat -> {
				Optional<LedgerAccountType> found = ledgerAccountTypeRepo
						.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(rat.getValue(),
								chartOfAccount.getValidFrom(), chartOfAccount.getValidFrom());
				if (!found.isPresent()) {
					// Create the two root ledger account types.
					LedgerAccountType lat = LedgerAccountType.builder().coa(coa.getName()).created(coa.getCreated())
							.user(coa.getUser()).validFrom(coa.getValidFrom()).name(rat.getValue())
							.parent(rat.getValue()).level(0).id(Ids.id()).build();
					ledgerAccountTypeRepo.save(lat);
				}
			});
		}

		return ChartOfAccount.clone(coa);
	}

	@Override
	public List<ChartOfAccount> findChartOfAccountsByName(ChartOfAccountName name) {
		List<ChartOfAccount> found = chartOfAccountRepo.findByName(name.getValue());
		return ChartOfAccount.clone(found);
	}

	@Override
	public Optional<ChartOfAccount> findChartOfAccountByName(ChartOfAccountName name, LocalDateTime referenceDate) {
		ChartOfAccount coa = chartOfAccountRepo
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name.getValue(),
						referenceDate, referenceDate)
				.orElse(null);
		return Optional.ofNullable(ChartOfAccount.clone(coa));
	}

	/**
	 * First check is ledger account with name is in database. If yes, set valid
	 * to.
	 */
	@Override
	public LedgerAccountType newLedgerAccountType(LedgerAccountType parent, LedgerAccountTypeName name,
			LocalDateTime validFrom) {
		Optional<LedgerAccountType> existingOptions = ledgerAccountTypeRepo
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name.getValue(),
						validFrom, validFrom);
		if (existingOptions.isPresent()) {
			// Check if same parent. We do not allow changing tree structure.
			if (!existingOptions.get().getParent().equals(parent.getName())) {
				throw new IllegalArgumentException(String.format("Ledger account with name %s has parent of name %s. ",
						name.getValue(), parent.getName()));
			}
			// Set valid to
			LedgerAccountType rat = existingOptions.get();
			rat.setValidTo(validFrom);
			ledgerAccountTypeRepo.save(rat);
		}

		// Look for a valid parent.
		Optional<LedgerAccountType> parentOptions = ledgerAccountTypeRepo
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(parent.getName(),
						validFrom, validFrom);
		if (!parentOptions.isPresent())
			throw new IllegalStateException(
					String.format("Missing corrsponding parent with name %s and valid at date %s", parent, validFrom));

		// Store the ledger account type.
		LedgerAccountType lat = LedgerAccountType.newChildInstance(name.getValue(), validFrom, principal.getName(),
				parentOptions.get());
		LedgerAccountType saved = ledgerAccountTypeRepo.save(lat);
		return LedgerAccountType.clone(saved);
	}

	@Override
	public Optional<LedgerAccountType> findLedgerAccountType(LedgerAccountName name, LocalDateTime referenceDate) {
		LedgerAccountType lat = ledgerAccountTypeRepo
				.findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(name.getValue(),
						referenceDate, referenceDate).orElse(null);
		return Optional.ofNullable(LedgerAccountType.clone(lat));
	}

	@Override
	public List<LedgerAccountType> findLedgerAccountTypes(LedgerAccountName name) {
		List<LedgerAccountType> found = ledgerAccountTypeRepo.findByName(name.getValue());
		return LedgerAccountType.clone(found);
	}

	@Override
	public List<LedgerAccountType> findChildLedgerAccountTypes(LedgerAccountName parentName,
			LocalDateTime referenceDate) {
		List<LedgerAccountType> found = ledgerAccountTypeRepo
				.findByParentAndValidFromBeforeAndValidToAfterOrderByLevelDescValidFromDesc(parentName.getValue(),
						referenceDate, referenceDate);
		return LedgerAccountType.clone(found);
	}

	@Override
	public List<LedgerAccountType> findCoaLedgerAccountTypes(ChartOfAccountName coaName, LocalDateTime referenceDate) {
		List<LedgerAccountType> found = ledgerAccountTypeRepo
				.findByCoaAndValidFromBeforeAndValidToAfterOrderByLevelDescValidFromDesc(coaName.getValue(),
						referenceDate, referenceDate);
		return LedgerAccountType.clone(found);
	}
}
