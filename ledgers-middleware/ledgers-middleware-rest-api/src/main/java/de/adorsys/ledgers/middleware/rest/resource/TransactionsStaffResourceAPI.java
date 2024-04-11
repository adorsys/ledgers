/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.MockBookingDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.API_KEY;
import static de.adorsys.ledgers.middleware.rest.utils.Constants.OAUTH2;

@Tag(name = "LDG014 - Transactions Mock Upload (STAFF access)")
public interface TransactionsStaffResourceAPI {
    String BASE_PATH = "/staff-access/transactions";

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(summary = "Posts transactions to Ledgers")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PostMapping
    ResponseEntity<Map<String, String>> transactions(@RequestBody List<MockBookingDetails> data);
}
