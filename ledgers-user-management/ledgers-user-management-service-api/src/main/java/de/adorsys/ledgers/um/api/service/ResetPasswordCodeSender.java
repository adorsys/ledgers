package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;

public interface ResetPasswordCodeSender {
     SendCode sendCode(ResetPassword source);
}
