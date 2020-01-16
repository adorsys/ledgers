package de.adorsys.ledgers.um.impl.service.password;

import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.ResetPasswordCodeSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.RESET_PASSWORD_CODE_SENDING_ERROR;

@Service
@RequiredArgsConstructor
public class ResetPasswordEmailCodeSenderImpl implements ResetPasswordCodeSender {
    private static final String CAN_NOT_SEND_EMAIL_CODE = "Can't send email with code";

    @Value("${reset-password.email.subject}")
    private String subject;

    @Value("${reset-password.email.from}")
    private String from;

    @Value("${reset-password.email.template-message}")
    private String templateMessage;

    private final UserMailSender userMailSender;

    @Override
    public SendCode sendCode(ResetPassword source) {
        if (!userMailSender.send(subject, from, source.getEmail(), String.format(templateMessage, source.getCode()))) {
            throw UserManagementModuleException.builder()
                          .errorCode(RESET_PASSWORD_CODE_SENDING_ERROR)
                          .devMsg(CAN_NOT_SEND_EMAIL_CODE)
                          .build();
        }
        return new SendCode(true);
    }
}
