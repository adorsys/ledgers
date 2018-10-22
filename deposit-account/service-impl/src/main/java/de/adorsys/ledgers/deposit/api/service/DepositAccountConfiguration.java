package de.adorsys.ledgers.deposit.api.service;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages={"de.adorsys.ledgers.deposit"})
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan(basePackages="de.adorsys.ledgers.deposit.domain")
public class DepositAccountConfiguration {

}
