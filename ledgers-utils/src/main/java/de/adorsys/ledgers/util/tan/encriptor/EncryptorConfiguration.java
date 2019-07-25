package de.adorsys.ledgers.util.tan.encriptor;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptorConfiguration {
    @Value("${application.security.encryptorAlgorithm}")
    private String encryptorAlgorithm;
    @Value("${application.security.masterPassword}")
    private String masterPassword;

    @Bean
    public StandardPBEStringEncryptor stringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(masterPassword);
        encryptor.setAlgorithm(encryptorAlgorithm);
        return encryptor;
    }
}
