package de.adorsys.ledgers.um.impl.service.password;

import de.adorsys.ledgers.security.GenerateCode;
import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.um.db.domain.ResetPasswordEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.ResetPasswordRepository;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetPasswordEmailCodeGeneratorImplTest {
    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String LOGIN = "Login";
    private static final String EMAIL = "email";
    private static final String PASS = "pass";

    @InjectMocks
    private ResetPasswordEmailCodeGeneratorImpl service;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ResetPasswordRepository resetPasswordRepository;

    @Test
    void generateCode_present_code() {
        when(userRepository.findByLoginAndEmail(any(), any())).thenReturn(getUser(false));
        when(resetPasswordRepository.findByUserId(ID)).thenReturn(getResetEntity());
        GenerateCode result = service.generateCode(getResource());
        assertTrue(StringUtils.isNotBlank(result.getCode()));
        assertTrue(result.isGenerated());
    }

    @Test
    void generateCode_new_code() {
        when(userRepository.findByLoginAndEmail(any(), any())).thenReturn(getUser(false));
        when(resetPasswordRepository.findByUserId(ID)).thenReturn(Optional.empty());
        when(resetPasswordRepository.save(any())).thenReturn(new ResetPasswordEntity(ID, CODE, OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC)));
        GenerateCode result = service.generateCode(getResource());
        assertTrue(StringUtils.isNotBlank(result.getCode()));
        assertTrue(result.isGenerated());
    }

    private Optional<ResetPasswordEntity> getResetEntity() {
        return Optional.of(new ResetPasswordEntity(ID, CODE, OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC)));
    }

    @Test
    void generateCode_fail_user_nf() {
        when(userRepository.findByLoginAndEmail(any(), any())).thenReturn(Optional.empty());
        assertThrows(UserManagementModuleException.class, () -> service.generateCode(getResource()));
    }

    @Test
    void generateCode_fail_user_blocked() {
        when(userRepository.findByLoginAndEmail(any(), any())).thenReturn(getUser(true));
        assertThrows(UserManagementModuleException.class, () -> service.generateCode(getResource()));
    }

    private Optional<UserEntity> getUser(boolean blocked) {
        UserEntity entity = new UserEntity();
        entity.setId(ID);
        entity.setLogin(LOGIN);
        entity.setEmail(EMAIL);
        entity.setPin(PASS);
        entity.setBlocked(blocked);
        return Optional.of(entity);
    }

    private ResetPassword getResource() {
        ResetPassword r = new ResetPassword();
        r.setLogin(LOGIN);
        r.setEmail(EMAIL);
        r.setPhone("tel");
        r.setCode(CODE);
        r.setNewPassword("new pass");
        return r;
    }
}