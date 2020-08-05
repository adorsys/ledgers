package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.MockBookingDetails;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

//@Api(tags = "LDG014 - Transactions Mock Upload (STAFF access)")
public interface TransactionsStaffResourceAPI {
    String BASE_PATH = "/staff-access/transactions";

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(value = "Posts transactions to Ledgers", authorizations = @Authorization(value = "apiKey"))
    @PostMapping
    ResponseEntity<Map<String, String>> transactions(@RequestBody List<MockBookingDetails> data);
}
