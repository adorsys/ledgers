/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.middleware.impl.mockbank.MockBankConfigSource;

@Configuration
public class MockBankConfig {

    @Bean
    public ASPSPConfigSource configSource() {
        return new MockBankConfigSource();
    }
}
