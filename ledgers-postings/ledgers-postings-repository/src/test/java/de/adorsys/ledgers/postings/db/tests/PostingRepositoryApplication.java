/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.tests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import de.adorsys.ledgers.postings.db.EnablePostingsReporitory;

@SpringBootApplication
@EnablePostingsReporitory
@EnableJpaAuditing
public class PostingRepositoryApplication {
	public static void main(String[] args) {
		SpringApplication.run(PostingRepositoryApplication.class, args);
	}
}
