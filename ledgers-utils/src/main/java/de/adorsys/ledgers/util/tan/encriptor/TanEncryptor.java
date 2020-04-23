package de.adorsys.ledgers.util.tan.encriptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TanEncryptor {
    private final PBEStringCleanablePasswordEncryptor passwordEncryptor;

    public String decryptTan(String hash) {
        return passwordEncryptor.decrypt(hash);
    }

    public String encryptTan(String password) {
        log.info("TAN encryption stage, password: " + password);
        return passwordEncryptor.encrypt(password);
    }
}
