package de.adorsys.ledgers.rest.posting.controller;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.rest.exception.NotFoundRestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;

import java.net.URI;

@RestController
public class ChartOfAccountController {
    private final ChartOfAccountService chartOfAccountService;

    public ChartOfAccountController(ChartOfAccountService chartOfAccountService) {
        this.chartOfAccountService = chartOfAccountService;
    }

    @PostMapping(path = "/coas")
    public ResponseEntity<Void> newChartOfAccount(ChartOfAccountBO chartOfAccount, UriBuilder uri) {
        ChartOfAccountBO coa = chartOfAccountService.newChartOfAccount(chartOfAccount);
        URI location = uri.path(coa.getId()).build();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(path = "/coas/{id}")
    public ResponseEntity<ChartOfAccountBO> findChartOfAccountsById(@PathVariable("id") String id) {
        ChartOfAccountBO coa = chartOfAccountService.findChartOfAccountsById(id).orElseThrow(() -> new NotFoundRestException(id));
        return ResponseEntity.ok(coa);
    }

    /**
     * List all chart of accounts with the given name. These are generally different versions of the same chart of account.
     *
     * @param name
     * @return an empty list if no chart of account with the given name.
     */
    @GetMapping(path = "/coas", params = {"name"})
    public ResponseEntity<ChartOfAccountBO> findChartOfAccountsByName(@RequestParam(required = true, name = "name") String name) {
        ChartOfAccountBO coa = chartOfAccountService.findChartOfAccountsByName(name).orElseThrow(() -> new NotFoundRestException(name));
        return ResponseEntity.ok(coa);
    }
}
