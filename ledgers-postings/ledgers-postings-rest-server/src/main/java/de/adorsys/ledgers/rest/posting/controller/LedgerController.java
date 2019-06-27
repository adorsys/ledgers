package de.adorsys.ledgers.rest.posting.controller;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.rest.exception.NotFoundRestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.security.Principal;

@RestController
public class LedgerController {
    private final Principal principal;
    private final LedgerService ledgerService;

    public LedgerController(Principal principal, LedgerService ledgerService) {
        this.principal = principal;
        this.ledgerService = ledgerService;
    }

    /**
     * Creates a new Ledger.
     *
     * @param ledger Ledger object
     * @return void response with HttpStatus 201 if successful
     */
    @PostMapping(path = "/ledgers")
    public ResponseEntity<Void> newLedger(LedgerBO ledger, UriBuilder uri) {
        LedgerBO newLedger = ledgerService.newLedger(ledger);

        URI location = uri.path(newLedger.getId()).build();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(path = "/ledgers/{id}")
    public ResponseEntity<LedgerBO> findLedgerById(@PathVariable("id") String id) {
        LedgerBO ledger = ledgerService.findLedgerById(id).orElseThrow(() -> new NotFoundRestException(id));
        return ResponseEntity.ok(ledger);
    }

    /**
     * Find the ledger with the given name.
     *
     * @param ledgerName name of corresponding Ledger
     * @return Ledger object
     */
    @GetMapping(path = "/ledgers", params = {"ledgerName"})
    public ResponseEntity<LedgerBO> findLedgerByName(@RequestParam(name = "ledgerName") String ledgerName) {
        LedgerBO ledger = ledgerService.findLedgerByName(ledgerName).orElseThrow(() -> new NotFoundRestException(ledgerName));
        return ResponseEntity.ok(ledger);
    }

    /**
     * Create a new Ledger account.
     * <p>
     * While creating a ledger account, the parent hat to be specified.
     *
     * @param ledgerAccount Ledger account
     * @return Void response with 201 HttpStatus if successful
     */
    @PostMapping(path = "/accounts")
    public ResponseEntity<Void> newLedgerAccount(@RequestBody LedgerAccountBO ledgerAccount, UriBuilder uri) {
        LedgerAccountBO newLedgerAccount = ledgerService.newLedgerAccount(ledgerAccount, principal.getName());
        URI location = uri.path(newLedgerAccount.getId()).build();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(path = "/accounts/{id}")
    public ResponseEntity<LedgerAccountBO> findLedgerAccountById(@PathVariable("id") String id) {
        LedgerAccountBO la = ledgerService.findLedgerAccountById(id).orElseThrow(() -> new NotFoundRestException(id));
        return ResponseEntity.ok(la);
    }

    /**
     * Find the ledger account with the given ledger name and account name and reference date.
     *
     * @param ledgerName  name of ledger
     * @param accountName name of account
     * @return Ledger Account
     */
    @GetMapping(path = "/accounts", params = {"ledgerName", "accountName"})
    public ResponseEntity<LedgerAccountBO> findLedgerAccountByName(
            @RequestParam(name = "ledgerName") String ledgerName,
            @RequestParam(name = "accountName") String accountName) {
        LedgerBO ledger = new LedgerBO();
        ledger.setName(ledgerName);

        return ledgerAccount(ledger, accountName);
    }

    /**
     * Find the ledger account with the given name
     *
     * @param accountName name of corresponding account
     * @return Ledger account
     */
    @GetMapping(path = "/ledgers/{ledgerId}/accounts", params = {"accountName"})
    public ResponseEntity<LedgerAccountBO> findLedgerAccount(
            @PathVariable("ledgerId") String ledgerId,
            @RequestParam(name = "accountName") String accountName) {
        LedgerBO ledger = new LedgerBO();
        ledger.setId(ledgerId);
        return ledgerAccount(ledger, accountName);
    }

    private ResponseEntity<LedgerAccountBO> ledgerAccount(LedgerBO ledger, String accountName) {
        LedgerAccountBO ledgerAccount = ledgerService.findLedgerAccount(ledger, accountName);

        return ResponseEntity.ok(ledgerAccount);
    }
}
