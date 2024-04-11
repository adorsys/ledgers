/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.UNPROTECTED_ENDPOINT;

@Tag(name = "LDG001 - Application Management", description = "Application management")
public interface AppMgmtRestAPI {
    String BASE_PATH = "/management/app";

    @GetMapping("/ping")
    @Operation(tags = UNPROTECTED_ENDPOINT, summary = "Echo the server")
    ResponseEntity<String> ping();

    @PostMapping("/init")
    @Operation(summary = "Initializes the deposit account module.")
    ResponseEntity<Void> initApp();

    @PostMapping("/admin")
    @Operation(tags = UNPROTECTED_ENDPOINT, summary = "Creates the admin account. This is only done if the application has no account yet. Returns a bearer token admin can use to proceed with further operations.")
    ResponseEntity<Void> admin(@RequestBody UserTO adminUser);
}
