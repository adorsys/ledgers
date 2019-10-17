package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.oauth.GrantTypeTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthServerInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "LDG012 - Oauth authorisation")
public interface OauthRestApi {
    String BASE_PATH = "/oauth";

    @PostMapping("/authorise")
    @ApiOperation(value = "Get authorisation code")
    ResponseEntity<OauthCodeResponseTO> oauthCode(@RequestParam("login") String login, @RequestParam("pin") String pin, @RequestParam("redirect_uri") String redirectUri);

    @PostMapping("/token")
    @ApiOperation(value = "Get/refresh access token")
    ResponseEntity<BearerTokenTO> oauthToken(@RequestParam("grant_type") GrantTypeTO grantType, @RequestParam("code") String code);

    @GetMapping("/authorization-server")
    @ApiOperation(value = "Authorization server metadata request")
    ResponseEntity<OauthServerInfoTO> oauthServerInfo();
}
