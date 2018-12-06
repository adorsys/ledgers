package de.adorsys.ledgers.middleware.rest.config;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.context.WebApplicationContext;

import de.adorsys.ledgers.middleware.security.JWTAuthenticationFilter;
import de.adorsys.ledgers.middleware.security.TokenAuthenticationService;

//@Configuration
//@EnableWebSecurity(debug=true)
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class StatelessWebSecurityConfig extends WebSecurityConfigurerAdapter {
//	@Autowired
//	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
//	}
	@Autowired
    private TokenAuthenticationService tokenAuthenticationService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.addFilterBefore(new JWTAuthenticationFilter(tokenAuthenticationService), BasicAuthenticationFilter.class)
			.exceptionHandling()
			.authenticationEntryPoint(forbiddenEntryPoint())
			.and()
				.exceptionHandling().accessDeniedHandler(nonRedirectingAccessDeniedHandler())
			.and()
				.authorizeRequests().anyRequest().authenticated()
			.and()
				.securityContext().securityContextRepository(securityContextRepository())
			.and().httpBasic();
	}

	@Bean
	public SecurityContextRepository securityContextRepository() {
		HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
		repo.setSpringSecurityContextKey("CUSTOM");
		return repo;
	}

	@Bean
	public AuthenticationEntryPoint forbiddenEntryPoint() {
		return new Http403ForbiddenEntryPoint();
	}

	@Bean
	public AccessDeniedHandler nonRedirectingAccessDeniedHandler() {
		return new AccessDeniedHandlerImpl();
	}

	@Bean
    @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST,proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Principal getPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
