package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.service.MiddlewareResetPasswordService;
import de.adorsys.ledgers.security.*;
import de.adorsys.ledgers.um.api.service.ResetPasswordCodeGenerator;
import de.adorsys.ledgers.um.api.service.ResetPasswordCodeSender;
import de.adorsys.ledgers.um.api.service.ResetPasswordCodeVerifier;
import de.adorsys.ledgers.um.api.service.UpdatePasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiddlewareResetPasswordServiceImpl implements MiddlewareResetPasswordService {
    private final ResetPasswordCodeGenerator resetPasswordCodeGenerator;
    private final ResetPasswordCodeSender resetPasswordCodeSender;
    private final ResetPasswordCodeVerifier resetPasswordCodeVerifier;
    private final UpdatePasswordService resetPasswordService;

    @Override
    public SendCode sendCode(ResetPassword resetPassword) {
        GenerateCode result = resetPasswordCodeGenerator.generateCode(resetPassword);
        return resetPasswordCodeSender.sendCode(resetPassword.withCode(result.getCode()));
    }

    @Override
    public UpdatePassword updatePassword(ResetPassword resetPassword) {
        VerifyCode result = resetPasswordCodeVerifier.verifyCode(resetPassword.getCode());
        return resetPasswordService.updatePassword(result.getUserId(), resetPassword.getNewPassword());
    }
}
