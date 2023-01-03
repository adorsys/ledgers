package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;

import static de.adorsys.ledgers.app.server.auth.PermittedResources.*;

@KeycloakConfiguration
@RequiredArgsConstructor
public class WebSecurityConfigKeycloak extends KeycloakWebSecurityConfigurerAdapter {
    private final KeycloakAuthMapper authMapper;
    private final Environment environment;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
                .csrf().disable()//NOSONAR Reason -> we only work with our proprietary backend services
                .cors().disable()
                // .and()
                .authorizeRequests().antMatchers(APP_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(INDEX_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(SWAGGER_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(CONSOLE_WHITELIST).permitAll()
                .and()
                .authorizeRequests().antMatchers(ACTUATOR_WHITELIST).permitAll()
                .anyRequest()
                .authenticated();
        http.addFilterBefore(new DisableEndpointFilter(environment), BasicAuthenticationFilter.class);
    }


    @Bean
    @RequestScope
    public AccessToken getAccessToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) request.getUserPrincipal();
        KeycloakPrincipal<? extends KeycloakSecurityContext> principal = (KeycloakPrincipal<? extends KeycloakSecurityContext>) token.getPrincipal();
        KeycloakSecurityContext session = principal.getKeycloakSecurityContext();
        return session.getToken();
    }

    @Bean
    @RequestScope
    public AccessTokenTO getAccessTokenTO() {
        return auth()
                       .map(this::extractAccessToken)
                       .orElse(null);
    }

    @Bean
    @RequestScope
    public BearerTokenTO getBearerTokenTO() {
        return auth()
                       .map(this::extractBearerToken)
                       .orElse(null);
    }

    @Bean
    @RequestScope
    public Principal getPrincipal() {
        return auth().orElse(null);
    }

    /**
     * Return Authentication or empty
     *
     * @return Authentication or Optional.empty()
     */
    private static Optional<Authentication> auth() {
        return SecurityContextHolder.getContext() == null
                       ? Optional.empty()
                       : Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    private AccessTokenTO extractAccessToken(Authentication authentication) {
        RefreshableKeycloakSecurityContext credentials = (RefreshableKeycloakSecurityContext) authentication.getCredentials();
        return authMapper.toAccessToken(credentials);
    }

    private BearerTokenTO extractBearerToken(Authentication authentication) {
        RefreshableKeycloakSecurityContext credentials = (RefreshableKeycloakSecurityContext) authentication.getCredentials();
        return authMapper.toBearer(credentials.getToken(), credentials.getTokenString());
    }
}
