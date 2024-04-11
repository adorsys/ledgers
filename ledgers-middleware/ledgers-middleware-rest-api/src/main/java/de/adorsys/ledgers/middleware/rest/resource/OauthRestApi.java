/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthServerInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG06 - Oauth authorisation")
public interface OauthRestApi { //TODO Shall be removed after final migration to Keycloak
    String BASE_PATH = "/oauth";

    @PostMapping("/authorise")
    @Operation(summary = "Get authorisation code")
    ResponseEntity<OauthCodeResponseTO> oauthCode(@RequestParam(LOGIN) String login,
                                                  @RequestParam(PIN) String pin,
                                                  @RequestParam(REDIRECT_URI) String redirectUri);

    @PostMapping("/authorise/oauth")
    @Operation(summary = "Get authorisation code, with token")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<OauthCodeResponseTO> oauthCode(@RequestParam(REDIRECT_URI) String redirectUri);

    @PostMapping("/token")
    @Operation(summary = "Get/refresh access token")
    ResponseEntity<BearerTokenTO> oauthToken(@RequestParam(CODE) String code);

    @GetMapping("/authorization-server")
    @Operation(summary = "Authorization server metadata request")
    ResponseEntity<OauthServerInfoTO> oauthServerInfo();
}
