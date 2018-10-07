package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.utils.CloneUtils;
import de.adorsys.ledgers.utils.Ids;

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
}
