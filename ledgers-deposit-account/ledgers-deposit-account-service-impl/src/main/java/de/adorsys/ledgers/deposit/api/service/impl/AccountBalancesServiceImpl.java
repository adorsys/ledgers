package de.adorsys.ledgers.deposit.api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.domain.BalanceTypeBO;
import de.adorsys.ledgers.deposit.api.service.AccountBalancesService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.postings.api.domain.AccountStmtBO;
import de.adorsys.ledgers.postings.api.domain.BalanceSideBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingTraceBO;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;

@Service
public class AccountBalancesServiceImpl extends AbstractServiceImpl implements AccountBalancesService {
	
	private AccountStmtService accountStmtService;
	
	public AccountBalancesServiceImpl(DepositAccountConfigService depositAccountConfigService,
			LedgerService ledgerService, AccountStmtService accountStmtService) {
		super(depositAccountConfigService, ledgerService);
		this.accountStmtService = accountStmtService;
	}

	@Override
	public List<BalanceBO> getBalances(String iban) throws LedgerAccountNotFoundException {
		LedgerBO ledger = loadLedger();
		LedgerAccountBO ledgerAccountBO = new LedgerAccountBO();
		ledgerAccountBO.setName(iban);
		ledgerAccountBO.setLedger(ledger);
		List<BalanceBO> result = new ArrayList<>();		
		try {
			AccountStmtBO stmt = accountStmtService.readStmt(ledgerAccountBO, LocalDateTime.now());
			BalanceBO balanceBO = new BalanceBO();
			BalanceSideBO balanceSide = stmt.getAccount().getBalanceSide();
			AmountBO amount = new AmountBO();
			balanceBO.setAmount(amount);
			if(BalanceSideBO.Cr.equals(balanceSide)) {
				amount.setAmount(stmt.creditBalance());
			} else {
				amount.setAmount(stmt.creditBalance());
			}
			balanceBO.setBalanceType(BalanceTypeBO.AVAILABLE);
			PostingTraceBO youngestPst = stmt.getYoungestPst();
			balanceBO.setReferenceDate(stmt.getPstTime().toLocalDate());
			if(youngestPst!=null) {
				balanceBO.setLastChangeDateTime(youngestPst.getSrcPstTime());
				balanceBO.setLastCommittedTransaction(youngestPst.getSrcPstId());
			} else {
				balanceBO.setLastChangeDateTime(stmt.getPstTime());
			}
			result.add(balanceBO);
		} catch (LedgerNotFoundException | BaseLineException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

}
