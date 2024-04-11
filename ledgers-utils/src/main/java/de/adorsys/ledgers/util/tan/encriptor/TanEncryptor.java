/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.tan.encriptor;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TanEncryptor {
    private final PBEStringCleanablePasswordEncryptor passwordEncryptor;

    public String decryptTan(String hash) {
        return passwordEncryptor.decrypt(hash);
    }

    public String encryptTan(String password) {
        return passwordEncryptor.encrypt(password);
    }
}
