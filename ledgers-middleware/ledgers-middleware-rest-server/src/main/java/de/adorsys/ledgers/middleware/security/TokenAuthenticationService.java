package de.adorsys.ledgers.middleware.security;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
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
            debug(String.format("Header value '{}' is blank.", HEADER_KEY));
            return null;
        }

        // Accepts only Bearer token
        if(!StringUtils.startsWithIgnoreCase(headerValue, TOKEN_PREFIX)) {
            debug(String.format("Header value does not start with '$s'.", TOKEN_PREFIX));
            return null;
        }

        // Strip prefix
        String accessToken = StringUtils.substringAfterLast(headerValue, " ");

        AccessTokenTO token;
		try {
			token = onlineBankingService.validate(accessToken);
		} catch (UserNotFoundMiddlewareException e) {
            debug("User with token not found.");
            return null;
		}

        if (token==null) {
        	debug("Token is not valid.");
            return null;
        }

        // process roles
        List<GrantedAuthority> authorities = new ArrayList<>();

        if(token.getRole()!=null) {
        	authorities.add(new SimpleGrantedAuthority("ROLE_" + token.getRole().name()));
        }

        return new MiddlewareAuthentication(token.getSub(), token, authorities, accessToken);
    }
	
	private void debug(String s) {
        if(logger.isDebugEnabled()) {
        	logger.debug(s);
        }
	}
}
