package de.adorsys.ledgers.postingserver.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriBuilder;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.service.LedgerService;

@RestController
public class LedgerController {
	
	@Autowired
	private LedgerService ledgerService;
	
	/**
	 * Creates a new Ledger.
	 * 
	 * @param ledger
	 * @return
	 */
	@PostMapping(path = "/ledgers")
	public ResponseEntity<Void> newLedger(Ledger ledger, UriBuilder uri){
		Ledger newLedger = ledgerService.newLedger(ledger);
		URI location = uri.path(newLedger.getId()).build();
		return ResponseEntity.created(location).build();
	}
	
	@GetMapping(path = "/ledgers/{ledgerId}")
	public ResponseEntity<Ledger> findLedgerById(@PathParam("ledgerId")String ledgerId){
		Ledger ledger = ledgerService.findLedgerById(ledgerId).orElseThrow(() -> new ResourceNotFoundException(ledgerId));
		return ResponseEntity.ok(ledger);
	}
	
	/**
	 * Find the ledger with the given name. 
	 * 
	 * @param ledgerName
	 * @return
	 */
	@GetMapping(path = "/ledgers", params={"ledgerName"})
	public ResponseEntity<Ledger> findLedgerByName(@RequestParam(required=true, name="ledgerName")String ledgerName){
		Ledger ledger = ledgerService.findLedgerByName(ledgerName).orElseThrow(() -> new ResourceNotFoundException(ledgerName));
		return ResponseEntity.ok(ledger);
	}
	
	/**
	 * Create a new Ledger account.
	 * 
	 * While creating a ledger account, the parent hat to be specified.
	 * 
	 * @param ledgerAccount
	 * @return
	 */
	@PostMapping(path = "/accounts")
	public ResponseEntity<Void> newLedgerAccount(@RequestBody LedgerAccount ledgerAccount, UriBuilder uri){
		LedgerAccount newLedgerAccount = ledgerService.newLedgerAccount(ledgerAccount);
		URI location = uri.path(newLedgerAccount.getId()).build();
		return ResponseEntity.created(location).build();
	}

	@GetMapping(path = "/accounts/{id}")
	public ResponseEntity<LedgerAccount> findLedgerAccountById(@PathParam("id")String id){
		LedgerAccount la = ledgerService.findLedgerAccountById(id).orElseThrow(() -> new ResourceNotFoundException(id));
		return ResponseEntity.ok(la);
	}

	/**
	 * Find the ledger account with the given name 
	 * 
	 * @param accountName
	 * @return
	 */
	@GetMapping(path = "/ledgers/{ledgerId}/accounts", params={"accountName","referenceDate"})
	public ResponseEntity<LedgerAccount> findLedgerAccount(
			@PathParam("ledgerId")String ledgerId,
			@RequestParam(required=true, name="accountName")String accountName, 
			@RequestParam(required=true, name="referenceDate")LocalDateTime referenceDate){
		Ledger ledger = Ledger.builder().id(ledgerId).build();
		LedgerAccount la = ledgerService.findLedgerAccount(ledger, accountName, referenceDate).orElseThrow(() -> new ResourceNotFoundException(accountName + "#" + referenceDate.toString()));
		return ResponseEntity.ok(la);
	}

	/**
	 * Find the ledger account with the given ledger name and account name and reference date. 
	 * 
	 * @param ledgerName
	 * @param accountName
	 * @param referenceDate
	 * @return
	 */
	@GetMapping(path = "/accounts", params={"ledgerName", "accountName","referenceDate"})
	public ResponseEntity<LedgerAccount> findLedgerAccountByName(
			@RequestParam(required=true, name="ledgerName")String ledgerName,
			@RequestParam(required=true, name="accountName")String accountName, 
			@RequestParam(required=true, name="referenceDate")LocalDateTime referenceDate){
		Ledger ledger = Ledger.builder().name(ledgerName).build();
		LedgerAccount la = ledgerService.findLedgerAccount(ledger, accountName, referenceDate).orElseThrow(() -> new ResourceNotFoundException(accountName + "#" + referenceDate.toString()));
		return ResponseEntity.ok(la);
	}
	
	/**
	 * Loads all ledger accounts with the given name.
	 * 
	 * @param accountName
	 * @return
	 */
	@GetMapping(path = "/ledgers/{ledgerId}/accounts", params={"accountName"})
	public ResponseEntity<List<LedgerAccount>> findLedgerAccounts(
			@PathParam("ledgerId")String ledgerId,
			@RequestParam(required=true, name="accountName")String accountName){
		Ledger ledger = Ledger.builder().id(ledgerId).build();
		List<LedgerAccount> accounts = ledgerService.findLedgerAccounts(ledger, accountName);
		return ResponseEntity.ok(accounts);
	}

	@GetMapping(path = "/accounts", params={"ledgerName", "accountName"})
	public ResponseEntity<List<LedgerAccount>> findLedgerAccountsByName(
			@RequestParam(required=true, name="ledgerName")String ledgerName,
			@RequestParam(required=true, name="accountName")String accountName){
		Ledger ledger = Ledger.builder().name(ledgerName).build();
		List<LedgerAccount> accounts = ledgerService.findLedgerAccounts(ledger, accountName);
		return ResponseEntity.ok(accounts);
	}
}
