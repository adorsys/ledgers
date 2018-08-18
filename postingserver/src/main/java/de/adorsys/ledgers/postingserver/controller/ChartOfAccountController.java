package de.adorsys.ledgers.postingserver.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriBuilder;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;

@RestController
public class ChartOfAccountController {
	
	@Autowired
	private ChartOfAccountService chartOfAccountService;

	@PostMapping(path = "/coas")
	public ResponseEntity<Void> newChartOfAccount(NewChartOfAccountRequest newChartOfAccountRequest, UriBuilder uri){
		ChartOfAccount coa = chartOfAccountService.newChartOfAccount(
				newChartOfAccountRequest.getChartOfAccount(), newChartOfAccountRequest.getRootAccountTypes());
		URI location = uri.path(coa.getId()).build();
		return ResponseEntity.created(location).build();
	}

	@GetMapping(path = "/coas/{id}")
	public ResponseEntity<ChartOfAccount> findChartOfAccountsById(@PathVariable("id")String id){
		ChartOfAccount coa = chartOfAccountService.findChartOfAccountsById(id).orElseThrow(() -> new ResourceNotFoundException(id));
		return ResponseEntity.ok(coa);
	}
	
	/**
	 * List all chart of accounts with the given name. These are generally different versions of the same chart of account.
	 * 
	 * @param name
	 * @return an empty list if no chart of account with the given name.
	 */
	@GetMapping(path = "/coas", params={"name"})
	public ResponseEntity<ChartOfAccount> findChartOfAccountsByName(@RequestParam(required=true, name="name")String name){
		ChartOfAccount coa = chartOfAccountService.findChartOfAccountsByName(name).orElseThrow(() -> new ResourceNotFoundException(name));
		return ResponseEntity.ok(coa);
	}

	/**
	 * Create a new Ledger account type.
	 * 
	 * While creating a ledger account type, the parent hat to be specified.
	 * 
	 * @param ledgerAccountType
	 * @param name
	 * @param desc
	 * @return
	 */
	@PostMapping(path="/coas/{coaId}/lat")
	public ResponseEntity<Void> newLedgerAccountType(NewLedgerAccountTypeRequest request, UriBuilder uri){
		LedgerAccountType lat = chartOfAccountService.newLedgerAccountType(request.getParent(), request.getName(), request.getDesc());
		URI location = uri.path(lat.getId()).build();
		return ResponseEntity.created(location).build();
	}
	
	@GetMapping(path = "/lat/{id}")
	public ResponseEntity<LedgerAccountType> findLedgerAccountTypeById(@PathVariable("id")String id){
		LedgerAccountType lat = chartOfAccountService.findLedgerAccountTypeById(id).orElseThrow(() -> new ResourceNotFoundException(id));
		return ResponseEntity.ok(lat);
	}
	
	/**
	 * Find the ledger account type with the given name 
	 * 
	 * @param name
	 * @return
	 */
	@GetMapping(path = "/lats", params={"name"})
	public ResponseEntity<LedgerAccountType> findLedgerAccountType(@RequestParam(required=true, name="name")String name){
		LedgerAccountType lat = chartOfAccountService.findLedgerAccountType(name).orElseThrow(() -> new ResourceNotFoundException(name));
		return ResponseEntity.ok(lat);
	}

	/**
	 * Returns all valid children of this node.
	 * @param parentName
	 * @param referenceDate
	 * @return
	 */
	@GetMapping(path = "/lats/children", params={"name"})
	public ResponseEntity<List<LedgerAccountType>> findChildLedgerAccountTypes(@RequestParam(required=true, name="name")String name){
		List<LedgerAccountType> list = chartOfAccountService.findChildLedgerAccountTypes(name);
		return ResponseEntity.ok(list);
	}

	/**
	 * Return all valid ledger account types attached to this coa.
	 * 
	 * @param coaName
	 * @param referenceDate
	 * @return
	 */
	@GetMapping(path = "/coas/lats", params={"name"})
	public ResponseEntity<List<LedgerAccountType>> findCoaLedgerAccountTypes(@RequestParam(required=true, name="name")String name){
		List<LedgerAccountType> list = chartOfAccountService.findCoaLedgerAccountTypes(name);
		return ResponseEntity.ok(list);
	}

}
