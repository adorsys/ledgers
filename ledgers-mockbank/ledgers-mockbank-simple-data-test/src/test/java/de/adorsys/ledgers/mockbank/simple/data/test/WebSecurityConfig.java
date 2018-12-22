package de.adorsys.ledgers.mockbank.simple.data.test;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import de.adorsys.ledgers.mockbank.simple.data.test.web.DBFlushResource;
import de.adorsys.ledgers.mockbank.simple.data.test.web.DataUploadResource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests().antMatchers(
            		DataUploadResource.UPLOAD_MOCKBANK_DATA,
            		DBFlushResource.FLUSH_PATH).permitAll();
        http.csrf().disable();
    }
}
