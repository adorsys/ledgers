/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl.test;

import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import de.adorsys.ledgers.deposit.api.service.EnableDepositAccountService;
import de.adorsys.ledgers.postings.impl.EnablePostingService;

@SpringBootApplication
@EnableDepositAccountService
@EnablePostingService
@EnableJpaAuditing
@EnableFeignClients(basePackageClasses = ExchangeRateClient.class)
public class DepositAccountServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(DepositAccountServiceApplication.class, args);
	}
}
