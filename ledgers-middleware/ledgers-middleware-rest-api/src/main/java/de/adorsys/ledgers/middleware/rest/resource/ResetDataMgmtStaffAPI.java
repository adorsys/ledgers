package de.adorsys.ledgers.middleware.rest.resource;

import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Api(tags = "LDG011 - Reset test data (STAFF access)")
public interface ResetDataMgmtStaffAPI {
    String BASE_PATH = "/staff-access/data";

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
}
