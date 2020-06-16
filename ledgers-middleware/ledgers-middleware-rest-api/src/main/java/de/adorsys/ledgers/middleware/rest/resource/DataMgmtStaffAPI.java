package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;
import java.util.Set;

@Api(tags = "LDG011 - Data management (STAFF access)")
public interface DataMgmtStaffAPI {
    String BASE_PATH = "/staff-access/data";

    @DeleteMapping(value = "/transactions/{accountId}")
    @ApiOperation(value = "Removes all transactions for account", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> account(@PathVariable("accountId") String accountId);

    @DeleteMapping(value = "/account/{accountId}")
    @ApiOperation(value = "Removes account", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> depositAccount(@PathVariable("accountId") String accountId);

    @DeleteMapping(value = "/user/{userId}")
    @ApiOperation(value = "Removes user", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> user(@PathVariable("userId") String userId);

    @DeleteMapping(value = "/branch/{branchId}")
    @ApiOperation(value = "Removes all data related to TPP", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> branch(@PathVariable("branchId") String branchId);

    @ApiOperation(value = "Upload data to Ledgers (users, accounts, transactions, balances)", authorizations = @Authorization(value = "apiKey"))
    @PostMapping(value = "/upload")
    ResponseEntity<Void> uploadData(@RequestBody UploadedDataTO data);

    @ApiOperation(value = "Retrieve the currencies list supported by ASPSP", authorizations = @Authorization(value = "apiKey"))
    @GetMapping(value = "/currencies")
    ResponseEntity<Set<Currency>> currencies();

    @ApiOperation(value = "Get next free branch id for country")
    @PostMapping(value = "/branch")
    ResponseEntity<String> branchId(@RequestBody BbanStructure bbanStructure);

    @ApiOperation(value = "Create Recovery point", authorizations = @Authorization(value = "apiKey"))
    @PostMapping(value = "/point")
    ResponseEntity<Void> createPoint(@RequestBody RecoveryPointTO recoveryPoint);

    @ApiOperation(value = "Get all Recovery points related to current branch", authorizations = @Authorization(value = "apiKey"))
    @GetMapping(value = "/point/all")
    ResponseEntity<List<RecoveryPointTO>> getAllPoints();

    @ApiOperation(value = "Get Recovery point by id related to current branch", authorizations = @Authorization(value = "apiKey"))
    @GetMapping(value = "/point/{id}")
    ResponseEntity<RecoveryPointTO> getPoint(@PathVariable("id") Long id);

    @ApiOperation(value = "Deletes Recovery point by id related to current branch", authorizations = @Authorization(value = "apiKey"))
    @DeleteMapping(value = "/point/{id}")
    ResponseEntity<String> deletePoint(@PathVariable("id") Long id);
}
