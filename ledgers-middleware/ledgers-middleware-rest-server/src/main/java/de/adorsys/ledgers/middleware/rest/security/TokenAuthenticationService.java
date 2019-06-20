package de.adorsys.ledgers.middleware.rest.security;


import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.um.api.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class TokenAuthenticationService {
    private final Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_KEY = "Authorization";

    private final MiddlewareOnlineBankingService onlineBankingService;
    private final UserService userService;

    public TokenAuthenticationService(MiddlewareOnlineBankingService onlineBankingService, UserService userService) {
        this.onlineBankingService = onlineBankingService;
        this.userService = userService;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_KEY);
        if (StringUtils.isBlank(headerValue)) {
            debug(String.format("Header value '%s' is blank.", HEADER_KEY));
            return null;
        }

        // Accepts only Bearer token
        if (!StringUtils.startsWithIgnoreCase(headerValue, TOKEN_PREFIX)) {
            debug(String.format("Header value does not start with '%s'.", TOKEN_PREFIX));
            return null;
        }

        // Strip prefix
        String accessToken = StringUtils.substringAfterLast(headerValue, " ");

        BearerTokenTO bearerToken;
        try {
            bearerToken = onlineBankingService.validate(accessToken);
        } catch (UserNotFoundMiddlewareException | InsufficientPermissionMiddlewareException e) {
            debug("User with token not found.", e);
            return null;
        }

        if (bearerToken == null) {
            debug("Token is not valid.");
            return null;
        }

        // process roles
        List<GrantedAuthority> authorities = new ArrayList<>();

        AccessTokenTO accessTokenTO = bearerToken.getAccessTokenObject();
        UserRoleTO role = accessTokenTO.getRole();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }

        return new MiddlewareAuthentication(accessTokenTO.getSub(), bearerToken, authorities, userService);
    }

    private void debug(String s) {
        if (logger.isDebugEnabled()) {
            logger.debug(s);
        }
    }

    private void debug(String s, Throwable e) {
        if (logger.isDebugEnabled()) {
            logger.debug(s, e);
        }
    }
}
