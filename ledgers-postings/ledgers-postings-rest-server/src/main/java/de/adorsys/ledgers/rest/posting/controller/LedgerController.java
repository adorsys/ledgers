package de.adorsys.ledgers.rest.posting.controller;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.rest.exception.NotFoundRestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;

import javax.websocket.server.PathParam;
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
     * @param ledger
     * @return
     */
    @PostMapping(path = "/ledgers")
    public ResponseEntity<Void> newLedger(LedgerBO ledger, UriBuilder uri) {
        LedgerBO newLedger;
        try {
            newLedger = ledgerService.newLedger(ledger);
        } catch (LedgerNotFoundException | ChartOfAccountNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
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
     * @param ledgerName
     * @return
     */
    @GetMapping(path = "/ledgers", params = {"ledgerName"})
    public ResponseEntity<LedgerBO> findLedgerByName(@RequestParam(required = true, name = "ledgerName") String ledgerName) {
        LedgerBO ledger = ledgerService.findLedgerByName(ledgerName).orElseThrow(() -> new NotFoundRestException(ledgerName));
        return ResponseEntity.ok(ledger);
    }

    /**
     * Create a new Ledger account.
     * <p>
     * While creating a ledger account, the parent hat to be specified.
     *
     * @param ledgerAccount
     * @return
     */
    @PostMapping(path = "/accounts")
    public ResponseEntity<Void> newLedgerAccount(@RequestBody LedgerAccountBO ledgerAccount, UriBuilder uri) {
        LedgerAccountBO newLedgerAccount;
        try {
            newLedgerAccount = ledgerService.newLedgerAccount(ledgerAccount, principal.getName());
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
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
     * @param ledgerName
     * @param accountName
     * @return
     * @throws LedgerNotFoundException
     */
    @GetMapping(path = "/accounts", params = {"ledgerName", "accountName"})
    public ResponseEntity<LedgerAccountBO> findLedgerAccountByName(
            @RequestParam(required = true, name = "ledgerName") String ledgerName,
            @RequestParam(required = true, name = "accountName") String accountName) throws LedgerNotFoundException, LedgerAccountNotFoundException {
        LedgerBO ledger = new LedgerBO();
        ledger.setName(ledgerName);

        return ledgerAccount(ledger, accountName);
    }

    /**
     * Find the ledger account with the given name
     *
     * @param accountName
     * @return
     * @throws LedgerNotFoundException
     */
    @GetMapping(path = "/ledgers/{ledgerId}/accounts", params = {"accountName"})
    public ResponseEntity<LedgerAccountBO> findLedgerAccount(
            @PathParam("ledgerId") String ledgerId,
            @RequestParam(required = true, name = "accountName") String accountName) throws LedgerNotFoundException, LedgerAccountNotFoundException {
        LedgerBO ledger = new LedgerBO();
        ledger.setId(ledgerId);
        return ledgerAccount(ledger, accountName);
    }

    private ResponseEntity<LedgerAccountBO> ledgerAccount(LedgerBO ledger, String accountName) throws LedgerNotFoundException, LedgerAccountNotFoundException {
        LedgerAccountBO ledgerAccount = ledgerService.findLedgerAccount(ledger, accountName);

        return ResponseEntity.ok(ledgerAccount);
    }
}
