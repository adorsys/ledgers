package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.security.VerifyCode;

public interface ResetPasswordCodeVerifier {
    VerifyCode verifyCode(String code);
}
