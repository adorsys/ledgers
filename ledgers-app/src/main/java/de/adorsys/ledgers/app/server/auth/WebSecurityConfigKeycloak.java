/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.server.auth;

import de.adorsys.ledgers.keycloak.client.mapper.KeycloakAuthMapper;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Principal;
import java.util.Optional;

import static de.adorsys.ledgers.app.server.auth.PermittedResources.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfigKeycloak {

    private final KeycloakAuthMapper authMapper;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = new KeycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }


    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS);

        http
                .authorizeHttpRequests()
                .requestMatchers(INDEX_WHITELIST).permitAll()
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                .requestMatchers(CONSOLE_WHITELIST).permitAll()
                .requestMatchers(ACTUATOR_WHITELIST).permitAll()
                .requestMatchers(APP_WHITELIST).permitAll()
                .anyRequest()
                .authenticated()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().oauth2ResourceServer()
                .jwt().jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()).and()
                .and().cors().disable() // by default uses a Bean by the name of corsConfigurationSource
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable();

        return http.build();
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
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

    @Bean
    @RequestScope
    public Authentication getAuthentication() {
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
        Jwt credentials = (Jwt) authentication.getCredentials();
        return authMapper.toAccessTokenFromJwt(credentials);
    }

    private BearerTokenTO extractBearerToken(Authentication authentication) {
        Jwt credentials = (Jwt) authentication.getCredentials();
        return authMapper.toBearerTokenFromJwt(credentials);
    }
}