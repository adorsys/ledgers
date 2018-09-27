package de.adorsys.ledgers.postingserver.controller;

import java.net.URI;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
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
	public ResponseEntity<LedgerAccount> findLedgerAccountById(@PathVariable("id")String id){
		LedgerAccount la = ledgerService.findLedgerAccountById(id).orElseThrow(() -> new ResourceNotFoundException(id));
		return ResponseEntity.ok(la);
	}

	/**
	 * Find the ledger account with the given ledger name and account name and reference date. 
	 * 
	 * @param ledgerName
	 * @param accountName
	 * @return
	 * @throws NotFoundException 
	 */
	@GetMapping(path = "/accounts", params={"ledgerName", "accountName"})
	public ResponseEntity<LedgerAccount> findLedgerAccountByName(
			@RequestParam(required=true, name="ledgerName")String ledgerName,
			@RequestParam(required=true, name="accountName")String accountName) throws NotFoundException{
		Ledger ledger = Ledger.builder().name(ledgerName).build();
		return ledgerAccount(ledger, accountName);
	}
	
	/**
	 * Find the ledger account with the given name 
	 * 
	 * @param accountName
	 * @return
	 * @throws NotFoundException 
	 */
	@GetMapping(path = "/ledgers/{ledgerId}/accounts", params={"accountName"})
	public ResponseEntity<LedgerAccount> findLedgerAccount(
			@PathParam("ledgerId")String ledgerId,
			@RequestParam(required=true, name="accountName")String accountName) throws NotFoundException{
		Ledger ledger = Ledger.builder().id(ledgerId).build();
		return ledgerAccount(ledger, accountName);
	}

	private ResponseEntity<LedgerAccount> ledgerAccount(Ledger ledger, String accountName) throws NotFoundException {
		LedgerAccount ledgerAccount = ledgerService.findLedgerAccount(ledger, accountName)
			.orElseThrow(() -> new NotFoundException());
		return ResponseEntity.ok(ledgerAccount);
	}
}
