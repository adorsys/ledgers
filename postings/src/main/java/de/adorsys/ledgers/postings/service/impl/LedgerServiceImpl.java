package de.adorsys.ledgers.postings.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.adorsys.ledgers.postings.domain.AccountCategory;
import de.adorsys.ledgers.postings.domain.BalanceSide;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.utils.CloneUtils;
import de.adorsys.ledgers.utils.Ids;

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
		Ledger ledger = null;
		if(ledgerAccount.getParent()!=null){
			parentAccount = loadLedgerAccount(ledgerAccount.getParent());
			ledger = parentAccount.getLedger();
		} else {
			ledger = loadLedger(ledgerAccount.getLedger());
		}
		
		AccountCategory category = null;
		if(ledgerAccount.getCategory()!=null){
			category = ledgerAccount.getCategory();
		} else if (parentAccount!=null){
			category = parentAccount.getCategory();
		} else {
			throw new IllegalArgumentException(String.format("Missing category for: " + ledgerAccount.getShortDesc()));
		}
		
		BalanceSide bs = null;
		if(ledgerAccount.getBalanceSide()!=null){
			bs =  ledgerAccount.getBalanceSide();
		} else if (parentAccount!=null){
			bs = parentAccount.getBalanceSide();
		} else if (category!=null){
			bs = category.getDefaultBs();
		} else {
			throw new IllegalArgumentException(String.format("Missing category for: " + ledgerAccount.getShortDesc()));
		}

		LedgerAccount la = LedgerAccount.builder()
			.id(Ids.id())
			.created(LocalDateTime.now())
			.user(user)
			.shortDesc(ledgerAccount.getShortDesc())
			.longDesc(ledgerAccount.getLongDesc())
			.name(ledgerAccount.getName())
			.ledger(ledger)
			.coa(ledger.getCoa())
			.parent(parentAccount)
			.category(category)
			.balanceSide(bs)
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
