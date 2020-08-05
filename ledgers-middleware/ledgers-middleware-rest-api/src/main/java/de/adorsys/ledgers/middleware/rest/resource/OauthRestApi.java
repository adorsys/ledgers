package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.oauth.GrantTypeTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthServerInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "LDG06 - Oauth authorisation")
public interface OauthRestApi {
    String BASE_PATH = "/oauth";

    @PostMapping("/authorise")
    @Operation(summary = "Get authorisation code")
    ResponseEntity<OauthCodeResponseTO> oauthCode(@RequestParam("login") String login, @RequestParam("pin") String pin, @RequestParam("redirect_uri") String redirectUri);

    @PostMapping("/authorise/oauth")
    @Operation(summary = "Get authorisation code, with token", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<OauthCodeResponseTO> oauthCode(@RequestParam("redirect_uri") String redirectUri);

    @PostMapping("/token")
    @Operation(summary = "Get/refresh access token")
    ResponseEntity<BearerTokenTO> oauthToken(@RequestParam("grant_type") GrantTypeTO grantType, @RequestParam("code") String code);

    @GetMapping("/authorization-server")
    @Operation(summary = "Authorization server metadata request")
    ResponseEntity<OauthServerInfoTO> oauthServerInfo();
}
