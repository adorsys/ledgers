package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.oauth.GrantTypeTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOauthService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareResetResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@MiddlewareResetResource
@RequiredArgsConstructor
@RequestMapping(OauthRestApi.BASE_PATH)
public class OauthResources implements OauthRestApi {
    private final MiddlewareOauthService middlewareOauthService;

    @Override
    public ResponseEntity<OauthCodeResponseTO> oauthCode(String login, String pin, String redirectUri) {
        return ResponseEntity.ok(middlewareOauthService.oauthCode(login, pin, redirectUri));
    }

    @Override
    public ResponseEntity<BearerTokenTO> oauthToken(GrantTypeTO grantType, String code) {
        return ResponseEntity.ok(middlewareOauthService.oauthToken(code));
    }
}
