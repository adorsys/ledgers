package de.adorsys.ledgers.middleware.security;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;

@Service
public class TokenAuthenticationService {
    private final Logger logger = LoggerFactory.getLogger(TokenAuthenticationService.class);

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_KEY = "Authorization";

    private final MiddlewareOnlineBankingService onlineBankingService;
    
    public TokenAuthenticationService(MiddlewareOnlineBankingService onlineBankingService) {
		this.onlineBankingService = onlineBankingService;
	}

	public Authentication getAuthentication(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_KEY);
        if(StringUtils.isBlank(headerValue)) {
            if(logger.isDebugEnabled()) logger.debug("Header value '{}' is blank.", HEADER_KEY);
            return null;
        }

        // Accepts only Bearer token
        if(!StringUtils.startsWithIgnoreCase(headerValue, TOKEN_PREFIX)) {
            if(logger.isDebugEnabled()) logger.debug("Header value does not start with '{}'.", TOKEN_PREFIX);
            return null;
        }

        // Strip prefix
        String accessToken = StringUtils.substringAfterLast(headerValue, " ");

        UserTO userTO;
		try {
			userTO = onlineBankingService.validate(accessToken);
		} catch (UserNotFoundMiddlewareException e) {
            if(logger.isDebugEnabled()) logger.debug("User with token not found.");
            return null;
		}

        if (userTO==null) {
            if(logger.isDebugEnabled()) logger.debug("Token is not valid.");
            return null;
        }

        // process roles
        List<GrantedAuthority> authorities = new ArrayList<>();

//        List<String> roles = userTO.getRoles();
//        if (roles != null) {
//            for (String role : roles) {
//                authorities.add(new SimpleGrantedAuthority(role));
//            }
//        }

        return new BearerTokenAuthentication(userTO.getId(), userTO, authorities, accessToken);
    }
}
