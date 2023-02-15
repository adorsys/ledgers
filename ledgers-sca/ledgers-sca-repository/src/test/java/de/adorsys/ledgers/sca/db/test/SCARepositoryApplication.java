/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.db.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.sca.db.EnableSCARepository;

@SpringBootApplication
@EnableSCARepository
public class SCARepositoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(SCARepositoryApplication.class, args);
    }

}
