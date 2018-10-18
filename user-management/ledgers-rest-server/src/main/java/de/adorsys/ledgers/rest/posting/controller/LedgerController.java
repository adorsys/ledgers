package de.adorsys.ledgers.rest.posting.controller;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.service.LedgerService;
import de.adorsys.ledgers.rest.exception.NotFoundRestException;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;

import javax.websocket.server.PathParam;
import java.net.URI;

@RestController
@AllArgsConstructor
public class LedgerController {
	private final LedgerService ledgerService;

		/**
	 * Creates a new Ledger.
	 * 
	 * @param ledger
	 * @return
	 */
	@PostMapping(path = "/ledgers")
	public ResponseEntity<Void> newLedger(Ledger ledger, UriBuilder uri){
		Ledger newLedger;
		try {
			newLedger = ledgerService.newLedger(ledger);
		} catch (de.adorsys.ledgers.postings.exception.NotFoundException e) {
			throw new NotFoundRestException(e.getMessage());
		}
		URI location = uri.path(newLedger.getId()).build();
		return ResponseEntity.created(location).build();
	}
	
	@GetMapping(path = "/ledgers/{id}")
	public ResponseEntity<Ledger> findLedgerById(@PathVariable("id")String id){
		Ledger ledger = ledgerService.findLedgerById(id).orElseThrow(() -> new NotFoundRestException(id));
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
		Ledger ledger = ledgerService.findLedgerByName(ledgerName).orElseThrow(() -> new NotFoundRestException(ledgerName));
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
		LedgerAccount newLedgerAccount;
		try {
			newLedgerAccount = ledgerService.newLedgerAccount(ledgerAccount);
		} catch (de.adorsys.ledgers.postings.exception.NotFoundException e) {
			throw new NotFoundRestException(e.getMessage());
		}
		URI location = uri.path(newLedgerAccount.getId()).build();
		return ResponseEntity.created(location).build();
	}

	@GetMapping(path = "/accounts/{id}")
	public ResponseEntity<LedgerAccount> findLedgerAccountById(@PathVariable("id")String id){
		LedgerAccount la = ledgerService.findLedgerAccountById(id).orElseThrow(() -> new NotFoundRestException(id));
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
