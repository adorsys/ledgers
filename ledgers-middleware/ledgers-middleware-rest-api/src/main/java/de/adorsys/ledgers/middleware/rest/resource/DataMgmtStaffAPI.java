package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.Set;

@Api(tags = "LDG011 - Data management (STAFF access)")
public interface DataMgmtStaffAPI {
    String BASE_PATH = "/staff-access/data";
//TODO CONSIDER REFACTORING TO USE ACCOUNT-ID AS SIMPLE IBAN IS NOT A VALID IDENTIFIER ANYMORE https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/467
    @DeleteMapping(value = "/transactions/{iban}")
    @ApiOperation(value = "Removes all transactions for account", authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The user data record without the user pin."),
            @ApiResponse(code = 401, message = "Provided bearer token could not be verified."),
            @ApiResponse(code = 403, message = "Provided bearer token not qualified for this operation."),
            @ApiResponse(code = 404, message = "Account not found.")
    })
    ResponseEntity<Void> account(@PathVariable("iban") String iban);

    @DeleteMapping(value = "/branch/{branchId}")
    @ApiOperation(value = "Removes all data related to TPP", authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The user data record without the user pin."),
            @ApiResponse(code = 401, message = "Provided bearer token could not be verified."),
            @ApiResponse(code = 403, message = "Provided bearer token not qualified for this operation."),
            @ApiResponse(code = 404, message = "Tpp not found.")
    })
    ResponseEntity<Void> branch(@PathVariable("branchId") String branchId);

    @ApiOperation(value = "Upload data to Ledgers (users, accounts, transactions, balances)", authorizations = @Authorization(value = "apiKey"))
    @PostMapping(value = "/upload")
    ResponseEntity<Void> uploadData(@RequestBody UploadedDataTO data);

    @ApiOperation(value = "Retrieve the currencies list supported by ASPSP", authorizations = @Authorization(value = "apiKey"))
    @GetMapping(value = "/currencies")
    ResponseEntity<Set<Currency>> currencies();
}
