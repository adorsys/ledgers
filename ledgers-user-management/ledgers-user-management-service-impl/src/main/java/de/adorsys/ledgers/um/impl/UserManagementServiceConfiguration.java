package de.adorsys.ledgers.um.impl;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.um.db.EnableUserManagmentRepository;
import de.adorsys.ledgers.util.PasswordEnc;

@Configuration
@ComponentScan(basePackageClasses=UserManagementServiceBasePackage.class)
@EnableUserManagmentRepository
public class UserManagementServiceConfiguration {
	@Bean
	public PasswordEnc passwordEnc() {
		return new PasswordEnc();
	}
}