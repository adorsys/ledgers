package de.adorsys.ledgers.app.server;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.rest.security.JWTAuthenticationFilter;
import de.adorsys.ledgers.middleware.rest.security.MiddlewareAuthentication;
import de.adorsys.ledgers.middleware.rest.security.TokenAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.Principal;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenAuthenticationService tokenAuthenticationService;

    private final CorsConfigProperties corsConfigProperties;

    @Autowired
    public WebSecurityConfig(TokenAuthenticationService tokenAuthenticationService, CorsConfigProperties corsConfigProperties) {
        this.tokenAuthenticationService = tokenAuthenticationService;
        this.corsConfigProperties = corsConfigProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests().antMatchers(
                "/",
                "/management/app/admin",
                "/management/app/ping",
                "/users/login",
                "/users/register",
                "/users/loginForConsent",
                "/data-test/upload-mockbank-data",
                "/data-test/db-flush",
                "/staff-access/users/register",
                "/staff-access/users/login").permitAll()
                .and()
                .authorizeRequests().antMatchers("/index.css", "/img/*", "/favicon.ico").permitAll()
                .and()
                .authorizeRequests().antMatchers("/v2/api-docs", "/swagger-resources", "/swagger-ui.html", "/webjars/**").permitAll()
                .and()
                .authorizeRequests().antMatchers("/console/**").permitAll()
                .and()
                .cors()
                .and()
                .authorizeRequests().anyRequest().authenticated();
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.headers().frameOptions().disable();
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

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
        configuration.setAllowedOrigins(corsConfigProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsConfigProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsConfigProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsConfigProperties.getAllowCredentials());
        configuration.setMaxAge(corsConfigProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
