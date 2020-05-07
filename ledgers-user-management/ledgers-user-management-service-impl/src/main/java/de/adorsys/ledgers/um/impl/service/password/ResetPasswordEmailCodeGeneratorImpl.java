package de.adorsys.ledgers.um.impl.service.password;

import de.adorsys.ledgers.security.GenerateCode;
import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.ResetPasswordCodeGenerator;
import de.adorsys.ledgers.um.db.domain.ResetPasswordEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.ResetPasswordRepository;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.USER_IS_BLOCKED;
import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ResetPasswordEmailCodeGeneratorImpl implements ResetPasswordCodeGenerator {
    private static final String USER_WITH_LOGIN_AND_EMAIL_NOT_FOUND = "User with login=%s and email=%s not found";

    @Value("${reset-password.expiration-code-minutes:2}")
    private int expirationMinutes;

    private final UserRepository userRepository;
    private final ResetPasswordRepository resetPasswordRepository;

    @Override
    @Transactional
    public GenerateCode generateCode(ResetPassword source) {
        UserEntity user = userRepository.findByLoginAndEmail(source.getLogin(), source.getEmail())
                                  .orElseThrow(() -> UserManagementModuleException.builder()
                                                             .errorCode(USER_NOT_FOUND)
                                                             .devMsg(String.format(USER_WITH_LOGIN_AND_EMAIL_NOT_FOUND, source.getLogin(), source.getEmail()))
                                                             .build());

        OffsetDateTime expiryTime = OffsetDateTime.now()
                                            .plusMinutes(expirationMinutes);

        if (!user.isEnabled()) {
            throw UserManagementModuleException.builder()
                          .errorCode(USER_IS_BLOCKED)
                          .devMsg("User is blocked, cannot reset password.")
                          .build();
        }

        String code = UUID.randomUUID().toString();
        Optional<ResetPasswordEntity> resetPasswordEntity = resetPasswordRepository.findByUserId(user.getId());

        if (!resetPasswordEntity.isPresent()) {
            return new GenerateCode(resetPasswordRepository.save(new ResetPasswordEntity(user.getId(), code, expiryTime))
                                            .getCode());
        }
        ResetPasswordEntity existed = resetPasswordEntity.get();
        existed.setCode(code);
        existed.setExpiryTime(expiryTime);
        return new GenerateCode(code);
    }
}
