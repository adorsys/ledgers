package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.rest.security.JWTAuthenticationFilter;
import de.adorsys.ledgers.middleware.rest.security.MiddlewareAuthentication;
import de.adorsys.ledgers.middleware.rest.security.TokenAuthenticationService;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.annotation.RequestScope;

import java.security.Principal;
import java.util.Optional;

import static de.adorsys.ledgers.app.server.auth.PermittedResources.*;

//@Configuration
//@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final Environment environment;
    private final TokenAuthenticationService tokenAuthenticationService;
    private final UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests().antMatchers(APP_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(INDEX_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(SWAGGER_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(CONSOLE_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(ACTUATOR_WHITELIST).permitAll()
                .and()
                .cors()
                .and()
                .authorizeRequests().anyRequest().authenticated();
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();
        http.addFilterBefore(new DisableEndpointFilter(environment), BasicAuthenticationFilter.class);
        http.addFilterBefore(new JWTAuthenticationFilter(tokenAuthenticationService), BasicAuthenticationFilter.class);
    }

    @Bean
    @RequestScope
    public Principal getPrincipal() {
        return auth().orElse(null);
    }

    @Bean
    @RequestScope
    public AccessTokenTO getAccessTokenTO() {
        return auth().map(this::extractToken).orElse(null);
    }

    /**
     * Return Authentication or empty
     *
     * @return
     */
    private static Optional<MiddlewareAuthentication> auth() {
        return SecurityContextHolder.getContext() == null
                       ? Optional.empty()
                       : Optional.ofNullable((MiddlewareAuthentication) SecurityContextHolder.getContext().getAuthentication());
    }

    private AccessTokenTO extractToken(MiddlewareAuthentication authentication) {
        return authentication.getBearerToken().getAccessTokenObject();
    }
}
