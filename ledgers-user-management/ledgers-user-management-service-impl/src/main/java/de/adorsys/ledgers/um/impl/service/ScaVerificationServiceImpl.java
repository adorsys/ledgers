package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.api.service.ScaVerificationService;
import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.repository.EmailVerificationRepository;
import de.adorsys.ledgers.um.impl.converter.EmailVerificationMapper;
import de.adorsys.ledgers.um.impl.service.password.UserMailSender;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.INVALID_VERIFICATION_TOKEN;
import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.TOKEN_NOT_FOUND;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScaVerificationServiceImpl implements ScaVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailVerificationMapper emailVerificationMapper;
    private final UserMailSender userMailSender;

    @Override
    public EmailVerificationBO findByScaIdAndStatusNot(String scaId, EmailVerificationStatusBO status) {
        EmailVerificationEntity entity = emailVerificationRepository.findByScaUserDataIdAndStatusNot(scaId, emailVerificationMapper.toEmailVerificationStatus(status))
                                                 .orElseThrow(() -> UserManagementModuleException.builder()
                                                                            .errorCode(TOKEN_NOT_FOUND)
                                                                            .devMsg("Verification token not found")
                                                                            .build());
        return emailVerificationMapper.toEmailVerificationBO(entity);
    }

    @Override
    public EmailVerificationBO findByToken(String token) {
        EmailVerificationEntity entity = emailVerificationRepository.findByToken(token)
                                                            .orElseThrow(() -> UserManagementModuleException.builder()
                                                                                       .errorCode(TOKEN_NOT_FOUND)
                                                                                       .devMsg(String.format("Verification token not found: %s", token))
                                                                                       .build());
        return emailVerificationMapper.toEmailVerificationBO(entity);
    }

    @Override
    public EmailVerificationBO findByTokenAndStatus(String token, EmailVerificationStatusBO statusBO) {
        EmailVerificationEntity verificationToken = emailVerificationRepository.findByTokenAndStatus(token, emailVerificationMapper.toEmailVerificationStatus(statusBO))
                                                            .orElseThrow(() -> UserManagementModuleException.builder()
                                                                                       .errorCode(INVALID_VERIFICATION_TOKEN)
                                                                                       .devMsg(String.format("Invalid verification token %s or email is already confirmed", token))
                                                                                       .build());
        return emailVerificationMapper.toEmailVerificationBO(verificationToken);
    }

    @Override
    public void updateEmailVerification(EmailVerificationBO emailVerificationBO) {
        emailVerificationRepository.save(emailVerificationMapper.toEmailVerificationEntity(emailVerificationBO));
    }

    @Override
    public boolean sendMessage(String subject, String from, String email, String message) {
        return userMailSender.send(subject, from, email, message);
    }
}