/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.postings.impl.EnablePostingService;

@SpringBootApplication
@EnablePostingService
public class PostingsApplication {
	public static void main(String[] args) {
		SpringApplication.run(PostingsApplication.class, args);
	}
}
