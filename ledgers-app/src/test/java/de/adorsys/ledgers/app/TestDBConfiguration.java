/*
 * Copyright (c) 2018-2025 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration
@EntityScan("de.adorsys.psd2.consent.domain")
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestDBConfiguration {

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDropFirst(true);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:/liquibase-master-changelog.xml");
        return liquibase;
    }
}
