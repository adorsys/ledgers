package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;
import de.adorsys.ledgers.security.UpdatePassword;

public interface MiddlewareResetPasswordService {
    SendCode sendCode(ResetPassword resetPassword);

    UpdatePassword updatePassword(ResetPassword resetPassword);
}
