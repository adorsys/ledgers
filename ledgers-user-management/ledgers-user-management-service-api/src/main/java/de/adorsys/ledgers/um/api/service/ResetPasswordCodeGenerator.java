package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.security.GenerateCode;
import de.adorsys.ledgers.security.ResetPassword;

public interface ResetPasswordCodeGenerator {
    GenerateCode generateCode(ResetPassword source);
}
