package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.service.EmailVerificationService;
import de.adorsys.ledgers.middleware.impl.config.EmailVerificationProperties;
import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.service.ScaUserDataService;
import de.adorsys.ledgers.um.api.service.ScaVerificationService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.EXPIRED_TOKEN;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final EmailVerificationStatusBO STATUS_VERIFIED = EmailVerificationStatusBO.VERIFIED;
    private static final EmailVerificationStatusBO STATUS_PENDING = EmailVerificationStatusBO.PENDING;

    private final ScaVerificationService scaVerificationService;
    private final ScaUserDataService scaUserDataService;
    private final EmailVerificationProperties configProperties;

    @Override
    public String createVerificationToken(String email) {
        ScaUserDataBO scaUserDataBO = scaUserDataService.findByEmail(email);
        EmailVerificationBO emailVerification;
        try {
            emailVerification = scaVerificationService.findByScaIdAndStatusNot(scaUserDataBO.getId(), STATUS_VERIFIED)
                                        .updateExpiration();
        } catch (UserManagementModuleException e) {
            emailVerification = new EmailVerificationBO(scaUserDataBO);
        }
        scaVerificationService.updateEmailVerification(emailVerification);
        return emailVerification.getToken();
    }

    @Override
    public void sendVerificationEmail(String token) {
        EmailVerificationBO emailVerificationBO = scaVerificationService.findByToken(token);
        ScaUserDataBO scaUserDataBO = emailVerificationBO.getScaUserData();
        scaVerificationService.sendMessage(configProperties.getTemplate().getSubject(),
                                           configProperties.getTemplate().getFrom(),
                                           scaUserDataBO.getMethodValue(),
                                           emailVerificationBO.formatMessage(
                                                   configProperties.getTemplate().getMessage(),
                                                   configProperties.getExtBasePath(),
                                                   configProperties.getEndPoint(),
                                                   emailVerificationBO.getToken(),
                                                   emailVerificationBO.getExpiredDateTime())
        );
    }

    @Override
    public void confirmUser(String token) {
        EmailVerificationBO emailVerification = scaVerificationService.findByTokenAndStatus(token, STATUS_PENDING);
        if (emailVerification.isExpired()) {
            throw UserManagementModuleException.builder()
                          .errorCode(EXPIRED_TOKEN)
                          .devMsg(String.format("Verification token for email %s is expired for confirmation", emailVerification.getScaUserData().getMethodValue()))
                          .build();
        }

        ScaUserDataBO scaUserDataBO = scaUserDataService.findByEmail(emailVerification.getScaUserData().getMethodValue());
        scaUserDataBO.setValid(true);
        emailVerification.confirmVerification();
        scaVerificationService.updateEmailVerification(emailVerification);
        scaUserDataService.updateScaUserData(scaUserDataBO);
    }
}