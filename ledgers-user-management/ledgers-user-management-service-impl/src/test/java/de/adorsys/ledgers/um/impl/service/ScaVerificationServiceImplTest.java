package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import de.adorsys.ledgers.um.db.repository.EmailVerificationRepository;
import de.adorsys.ledgers.um.impl.converter.EmailVerificationMapper;
import de.adorsys.ledgers.um.impl.service.password.UserMailSender;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScaVerificationServiceImplTest {

    @InjectMocks
    private ScaVerificationServiceImpl verificationService;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private EmailVerificationMapper emailVerificationMapper;
    @Mock
    private UserMailSender userMailSender;

    private ResourceReader reader = YamlReader.getInstance();

    private static final String SCA_ID = "6DwJm-TpResvxLdX3fHpjc";
    private static final String TOKEN = "Fz-4Kb6vREgj38CpsUAtSI";
    private static final EmailVerificationStatusBO STATUS_BO_PENDING = EmailVerificationStatusBO.PENDING;
    private static final EmailVerificationStatus STATUS_PENDING = EmailVerificationStatus.PENDING;
    private static final EmailVerificationStatusBO STATUS_BO_VERIFIED = EmailVerificationStatusBO.VERIFIED;
    private static final EmailVerificationStatus STATUS_VERIFIED = EmailVerificationStatus.VERIFIED;
    private EmailVerificationEntity emailVerificationEntity;
    private EmailVerificationBO emailVerificationBO;

    @BeforeEach
    void setUp() {
        emailVerificationEntity = readEmailVerificationEntity();
        emailVerificationBO = readEmailVerificationBO();
    }

    @Test
    void findByScaIdAndStatusNot() {
        when(emailVerificationRepository.findByScaUserDataIdAndStatusNot(any(), any())).thenReturn(Optional.ofNullable(emailVerificationEntity));
        when(emailVerificationMapper.toEmailVerificationStatus(any())).thenReturn(STATUS_VERIFIED);
        when(emailVerificationMapper.toEmailVerificationBO(any())).thenReturn(emailVerificationBO);

        EmailVerificationBO emailVerificationBO = verificationService.findByScaIdAndStatusNot(SCA_ID, STATUS_BO_VERIFIED);
        assertThat(emailVerificationBO.getScaUserData().getId(), is(SCA_ID));
        assertThat(emailVerificationBO.getStatus(), is(STATUS_BO_PENDING));

        verify(emailVerificationRepository, times(1)).findByScaUserDataIdAndStatusNot(SCA_ID, STATUS_VERIFIED);
    }

    @Test
    void findByScaIdAndStatusNot_tokenNotFound() {
        // Given
        when(emailVerificationRepository.findByScaUserDataIdAndStatusNot(any(), any())).thenReturn(Optional.empty());
        when(emailVerificationMapper.toEmailVerificationStatus(any())).thenReturn(STATUS_VERIFIED);

        // Then
        assertThrows(UserManagementModuleException.class, () -> verificationService.findByScaIdAndStatusNot(SCA_ID, STATUS_BO_VERIFIED));
    }

    @Test
    void findByToken() {
        // Given
        when(emailVerificationRepository.findByToken(any())).thenReturn(Optional.ofNullable(emailVerificationEntity));
        when(emailVerificationMapper.toEmailVerificationBO(any())).thenReturn(emailVerificationBO);

        // When
        EmailVerificationBO emailVerificationBO = verificationService.findByToken(TOKEN);

        // Then
        assertThat(emailVerificationBO.getToken(), is(TOKEN));
        verify(emailVerificationRepository, times(1)).findByToken(TOKEN);
    }

    @Test
    void findByToken_tokenNotFound() {
        // Given
        when(emailVerificationRepository.findByToken(any())).thenReturn(Optional.empty());

        // Then
        assertThrows(UserManagementModuleException.class, () -> verificationService.findByToken(TOKEN));
    }

    @Test
    void findByTokenAndStatus() {
        // Given
        when(emailVerificationRepository.findByTokenAndStatus(any(), any())).thenReturn(Optional.ofNullable(emailVerificationEntity));
        when(emailVerificationMapper.toEmailVerificationStatus(any())).thenReturn(STATUS_PENDING);
        when(emailVerificationMapper.toEmailVerificationBO(any())).thenReturn(emailVerificationBO);

        // When
        EmailVerificationBO emailVerificationBO = verificationService.findByTokenAndStatus(TOKEN, STATUS_BO_PENDING);

        // Then
        assertThat(emailVerificationBO.getToken(), is(TOKEN));
        assertThat(emailVerificationBO.getStatus(), is(STATUS_BO_PENDING));

        verify(emailVerificationRepository, times(1)).findByTokenAndStatus(TOKEN, STATUS_PENDING);
    }

    @Test
    void findByTokenAndStatus_tokenNotFound() {
        // Given
        when(emailVerificationRepository.findByTokenAndStatus(any(), any())).thenReturn(Optional.empty());
        when(emailVerificationMapper.toEmailVerificationStatus(any())).thenReturn(STATUS_PENDING);

        // Then
        assertThrows(UserManagementModuleException.class, () -> verificationService.findByTokenAndStatus(TOKEN, STATUS_BO_PENDING));
    }

    @Test
    void updateEmailVerification() {
        // Given
        when(emailVerificationMapper.toEmailVerificationEntity(any())).thenReturn(emailVerificationEntity);

        // When
        verificationService.updateEmailVerification(emailVerificationBO);

        // Then
        verify(emailVerificationRepository, times(1)).save(emailVerificationEntity);
    }

    @Test
    void sendMessage() {
        when(userMailSender.send(any(), any(), any(), any())).thenReturn(true);

        Assertions.assertTrue(verificationService.sendMessage("subject", "from", "email", "message"));
    }

    private EmailVerificationEntity readEmailVerificationEntity() {
        try {
            return reader.getObjectFromResource(getClass(), "email-verification.yml", EmailVerificationEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private EmailVerificationBO readEmailVerificationBO() {
        try {
            return reader.getObjectFromResource(getClass(), "email-verification.yml", EmailVerificationBO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
