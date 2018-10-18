package de.adorsys.ledgers.rest.posting.controller;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import de.adorsys.ledgers.rest.exception.NotFoundRestException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
public class ChartOfAccountController {
    private final ChartOfAccountService chartOfAccountService;

    @PostMapping(path = "/coas")
    public ResponseEntity<Void> newChartOfAccount(ChartOfAccount chartOfAccount, UriBuilder uri) {
        ChartOfAccount coa = chartOfAccountService.newChartOfAccount(chartOfAccount);
        URI location = uri.path(coa.getId()).build();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(path = "/coas/{id}")
    public ResponseEntity<ChartOfAccount> findChartOfAccountsById(@PathVariable("id") String id) {
        ChartOfAccount coa = chartOfAccountService.findChartOfAccountsById(id).orElseThrow(() -> new NotFoundRestException(id));
        return ResponseEntity.ok(coa);
    }

    /**
     * List all chart of accounts with the given name. These are generally different versions of the same chart of account.
     *
     * @param name
     * @return an empty list if no chart of account with the given name.
     */
    @GetMapping(path = "/coas", params = {"name"})
    public ResponseEntity<ChartOfAccount> findChartOfAccountsByName(@RequestParam(required = true, name = "name") String name) {
        ChartOfAccount coa = chartOfAccountService.findChartOfAccountsByName(name).orElseThrow(() -> new NotFoundRestException(name));
        return ResponseEntity.ok(coa);
    }
}
