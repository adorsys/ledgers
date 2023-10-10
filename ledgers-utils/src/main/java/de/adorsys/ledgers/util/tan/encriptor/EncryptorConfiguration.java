/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.tan.encriptor;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EncryptorConfiguration {
    @Value("${ledgers.application.security.encryptorAlgorithm}")
    private String encryptorAlgorithm;
    @Value("${ledgers.application.security.masterPassword}")
    private String masterPassword;

    @Bean
    public StandardPBEStringEncryptor stringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        log.info("Algorithm: {}", encryptorAlgorithm);
        log.debug("masterPass: {}", masterPassword);
        encryptor.setPassword(masterPassword);
        encryptor.setAlgorithm(encryptorAlgorithm);
        return encryptor;
    }
}
