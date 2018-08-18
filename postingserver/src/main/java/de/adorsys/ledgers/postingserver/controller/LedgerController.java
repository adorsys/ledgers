package de.adorsys.ledgers.postingserver.controller;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	
	@GetMapping(path = "/ledgers/{id}")
	public ResponseEntity<Ledger> findLedgerById(@PathVariable("id")String id){
		Ledger ledger = ledgerService.findLedgerById(id).orElseThrow(() -> new ResourceNotFoundException(id));
		return ResponseEntity.ok(ledger);
	}
	
	/**
	 * List all ledgers with the given name. These are generally different versions of the same ledger.
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping(path = "/ledgers", params={"name"})
	public ResponseEntity<Ledger> findLedgerByName(@RequestParam(required=true, name="name")String name){
		Ledger ledger = ledgerService.findLedgerByName(name).orElseThrow(() -> new ResourceNotFoundException(name));
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
	public ResponseEntity<LedgerAccount> findLedgerAccountById(@PathVariable("id")String id){
		LedgerAccount la = ledgerService.findLedgerAccountById(id).orElseThrow(() -> new ResourceNotFoundException(id));
		return ResponseEntity.ok(la);
	}
	
	/**
	 * Find the ledger account with the given name 
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping(path = "/accounts", params={"name","referenceDate"})
	public ResponseEntity<LedgerAccount> findLedgerAccount(
			@RequestParam(required=true, name="name")String name, 
			@RequestParam(required=true, name="referenceDate")LocalDateTime referenceDate){
		LedgerAccount la = ledgerService.findLedgerAccount(name, referenceDate).orElseThrow(() -> new ResourceNotFoundException(name + "#" + referenceDate.toString()));
		return ResponseEntity.ok(la);
	}

	/**
	 * Loads all ledger accounts with the given name.
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping(path = "/accounts", params={"name"})
	public ResponseEntity<List<LedgerAccount>> findLedgerAccounts(@RequestParam(required=true, name="name")String name){
		List<LedgerAccount> accounts = ledgerService.findLedgerAccounts(name);
		return ResponseEntity.ok(accounts);
	}
}
