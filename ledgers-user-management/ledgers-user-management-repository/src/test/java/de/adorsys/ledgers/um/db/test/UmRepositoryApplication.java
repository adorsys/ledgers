/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.adorsys.ledgers.um.db.EnableUserManagmentRepository;

@SpringBootApplication
@EnableUserManagmentRepository
public class UmRepositoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(UmRepositoryApplication.class, args);
    }
}
