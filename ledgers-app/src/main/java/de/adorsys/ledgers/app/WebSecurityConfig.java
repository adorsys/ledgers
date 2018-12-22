package de.adorsys.ledgers.app;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.rest.security.JWTAuthenticationFilter;
import de.adorsys.ledgers.middleware.rest.security.TokenAuthenticationService;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests().antMatchers(
            		"/",
            		"/management/app/admin",
            		"/management/app/ping", 
            		"/users/authorise2", 
            		"/users/authorise",
            		"/data-test/upload-mockbank-data",
            		"/data-test/db-flush",
            		"/users/register").permitAll()
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
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST,proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Principal getPrincipal() {
		return auth().orElse(null);
    }

	@Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST,proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AccessTokenTO getAccessTokenTO() {
        return auth().map(this::extractToken).orElse(null);
    }
	
	/**
	 * Return Authentication or empty
	 * 
	 * @return
	 */
	private static Optional<Authentication> auth(){
		return SecurityContextHolder.getContext()==null
				? Optional.empty()
				: Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
	}
	
	private AccessTokenTO extractToken(Authentication authentication) {
		Object credentials = authentication.getCredentials();
		if(credentials instanceof AccessTokenTO) {
			return (AccessTokenTO) credentials;
		}
		return null;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		return source;
	}
}
