package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.postings.utils.CloneUtils;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.postings.exception.NotFoundException;

@Service
@Transactional
public class LedgerServiceImpl extends AbstractServiceImpl implements LedgerService {

	/**
	 * Creates a new ledger.
	 * @throws NotFoundException 
	 */
	@Override
	public Ledger newLedger(Ledger ledger) throws NotFoundException {
		ChartOfAccount coa = loadCoa(ledger.getCoa());
		LocalDateTime created = LocalDateTime.now();
		String user = principal.getName();
		Ledger newLedger = new Ledger(Ids.id(), created, user, ledger.getShortDesc(), ledger.getLongDesc(), ledger.getName(),coa, ledger.getLastClosing());
		Ledger savedLedger = ledgerRepository.save(newLedger);

		return CloneUtils.cloneObject(savedLedger, Ledger.class);
	}

	@Override
	public Optional<Ledger> findLedgerById(String id) {
		return Optional.ofNullable(CloneUtils.cloneObject(ledgerRepository.findById(id).orElse(null), Ledger.class));
	}

	@Override
	public Optional<Ledger> findLedgerByName(String name) {
		return Optional.ofNullable(CloneUtils.cloneObject(ledgerRepository.findOptionalByName(name).orElse(null), Ledger.class));
	}

	@Override
	public LedgerAccount newLedgerAccount(LedgerAccount ledgerAccount) throws NotFoundException {
		// User
		String user = principal.getName();

		// Validations
		if(ledgerAccount.getName()==null)
			throw new IllegalArgumentException(String.format("Missing model name."));

		LedgerAccount parentAccount = null;
		if(ledgerAccount.getParent()!=null)
			parentAccount = loadLedgerAccount(ledgerAccount.getParent());

		// Check level Set it to 0 if root account type
		int level = parentAccount != null ? parentAccount.getLevel() + 1 : 0;

		// Load the ledger account type of ledger account
		LedgerAccountType accountType = loadAccountType(ledgerAccount.getAccountType());
		
		LedgerAccount la = LedgerAccount.builder()
			.id(Ids.id())
			.created(LocalDateTime.now())
			.user(user)
			.shortDesc(ledgerAccount.getShortDesc())
			.longDesc(ledgerAccount.getLongDesc())
			.name(ledgerAccount.getName())
			.ledger(parentAccount.getLedger())
			.parent(parentAccount)
			.accountType(accountType)
			.level(level)
			.build();

		return CloneUtils.cloneObject(ledgerAccountRepository.save(la), LedgerAccount.class);
	}

	@Override
	public Optional<LedgerAccount> findLedgerAccountById(String id) {
		return ledgerAccountRepository.findById(id);
	}

	@Override
	public Optional<LedgerAccount> findLedgerAccount(Ledger ledger, String name) {
		LedgerAccount ledgerAccount = ledgerAccountRepository
				.findOptionalByLedgerAndName(ledger, name).orElse(null);
		return Optional.ofNullable(CloneUtils.cloneObject(ledgerAccount, LedgerAccount.class));
	}
}
