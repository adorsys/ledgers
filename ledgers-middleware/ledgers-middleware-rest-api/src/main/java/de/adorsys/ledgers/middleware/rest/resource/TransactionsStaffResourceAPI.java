package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.MockBookingDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Tag(name = "LDG014 - Transactions Mock Upload (STAFF access)")
public interface TransactionsStaffResourceAPI {
    String BASE_PATH = "/staff-access/transactions";

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(summary = "Posts transactions to Ledgers"/*, authorizations = @Authorization(value = "apiKey")*/)
    @PostMapping
    ResponseEntity<Map<String, String>> transactions(@RequestBody List<MockBookingDetails> data);
}
