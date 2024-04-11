/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.impl.config.EmailVerificationProperties;
import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.service.ScaUserDataService;
import de.adorsys.ledgers.um.api.service.ScaVerificationService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;
    @Mock
    private ScaVerificationService scaVerificationService;
    @Mock
    private ScaUserDataService scaUserDataService;
    @Mock
    private EmailVerificationProperties emailVerificationProperties;

    private final ResourceReader reader = YamlReader.getInstance();

    private static final String EMAIL = "google@gmail.com";
    private static final String VERIFICATION_TOKEN = "Fz-4Kb6vREgj38CpsUAtSI";
    private static final LocalDateTime date = LocalDateTime.now();
    private static final EmailVerificationStatusBO STATUS_PENDING = EmailVerificationStatusBO.PENDING;
    private ScaUserDataBO scaUserDataBO;

    @BeforeEach
    void setUp() {
        scaUserDataBO = readScaUserDataBO();
    }

    @Test
    void createVerificationToken() {
        // Given
        when(scaUserDataService.findByEmail(EMAIL)).thenReturn(scaUserDataBO);
        when(scaVerificationService.findByScaIdAndStatusNot(scaUserDataBO.getId(), EmailVerificationStatusBO.VERIFIED))
                .thenReturn(getEmailVerificationBO(date));

        // When
        String token = emailVerificationService.createVerificationToken(EMAIL);
        assertFalse(token.isEmpty());

        verify(scaVerificationService, times(1)).updateEmailVerification(any(EmailVerificationBO.class));
    }

    @Test
    void createVerificationToken_emailVerificationExists() {
        scaUserDataBO.setValid(true);
        when(scaUserDataService.findByEmail(EMAIL)).thenReturn(scaUserDataBO);
        when(scaVerificationService.findByScaIdAndStatusNot(scaUserDataBO.getId(), EmailVerificationStatusBO.VERIFIED))
                .thenThrow(UserManagementModuleException.builder().build());

        // When
        String token = emailVerificationService.createVerificationToken(EMAIL);
        assertFalse(token.isEmpty());
        assertFalse(scaUserDataBO.isValid());

        verify(scaVerificationService, times(1)).deleteByScaId(scaUserDataBO.getId());
        verify(scaUserDataService, times(1)).updateScaUserData(scaUserDataBO);
        verify(scaVerificationService, times(1)).updateEmailVerification(any(EmailVerificationBO.class));
    }

    @Test
    void sendVerificationEmail() {
        // Given
        when(scaVerificationService.findByToken(any())).thenReturn(getEmailVerificationBO(date));
        when(scaVerificationService.sendMessage(any(), any(), any(), any())).thenReturn(true);
        when(emailVerificationProperties.getTemplate()).thenReturn(getEmailTemplate());

        // When
        emailVerificationService.sendVerificationEmail(VERIFICATION_TOKEN);
        verify(scaVerificationService, times(1)).sendMessage(any(), any(), any(), any());
    }

    @Test
    void confirmUser() {
        // Given
        when(scaVerificationService.findByTokenAndStatus(any(), any())).thenReturn(getEmailVerificationBO(date.plusWeeks(1)));
        when(scaUserDataService.findByEmail(any())).thenReturn(scaUserDataBO);

        // When
        emailVerificationService.confirmUser(VERIFICATION_TOKEN);
        verify(scaUserDataService, times(1)).updateScaUserData(any());
    }

    @Test
    void confirmUser_expiredToken() {
        // Given
        when(scaVerificationService.findByTokenAndStatus(any(), any())).thenReturn(getEmailVerificationBO(date.minusWeeks(1)));

        // Then
        assertThrows(UserManagementModuleException.class, () -> emailVerificationService.confirmUser(VERIFICATION_TOKEN));
    }

    private ScaUserDataBO readScaUserDataBO() {
        try {
            return reader.getObjectFromResource(getClass(), "sca-user-data.yml", ScaUserDataBO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private EmailVerificationBO getEmailVerificationBO(LocalDateTime date) {
        EmailVerificationBO bo = new EmailVerificationBO();
        bo.setToken(VERIFICATION_TOKEN);
        bo.setStatus(STATUS_PENDING);
        bo.setIssuedDateTime(LocalDateTime.now());
        bo.setExpiredDateTime(date);
        bo.setScaUserData(scaUserDataBO);
        return bo;
    }

    private EmailVerificationProperties.EmailTemplate getEmailTemplate() {
        EmailVerificationProperties.EmailTemplate template = new EmailVerificationProperties.EmailTemplate();
        template.setSubject("Please verify your email address");
        template.setFrom("noreply@adorsys.de");
        template.setMessage("example");
        return template;
    }
}