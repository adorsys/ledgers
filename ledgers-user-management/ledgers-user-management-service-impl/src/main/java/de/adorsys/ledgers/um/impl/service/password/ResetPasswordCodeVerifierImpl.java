package de.adorsys.ledgers.um.impl.service.password;

import de.adorsys.ledgers.security.VerifyCode;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.ResetPasswordCodeVerifier;
import de.adorsys.ledgers.um.db.domain.ResetPasswordEntity;
import de.adorsys.ledgers.um.db.repository.ResetPasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.RESET_PASSWORD_CODE_INVALID;

@Service
@RequiredArgsConstructor
public class ResetPasswordCodeVerifierImpl implements ResetPasswordCodeVerifier {
    private static final String INVALID_CODE = "Reset password code is invalid";
    private static final String CODE_EXPIRED = "Reset password code is expired";

    private final ResetPasswordRepository resetPasswordRepository;

    @Override
    public VerifyCode verifyCode(String code) {
        ResetPasswordEntity resetPasswordEntity = resetPasswordRepository.findByCode(code)
                                                          .orElseThrow(() -> UserManagementModuleException.builder()
                                                                                     .errorCode(RESET_PASSWORD_CODE_INVALID)
                                                                                     .devMsg(INVALID_CODE)
                                                                                     .build());
        if (resetPasswordEntity.isExpired()) {
            throw UserManagementModuleException.builder()
                          .errorCode(RESET_PASSWORD_CODE_INVALID)
                          .devMsg(CODE_EXPIRED)
                          .build();
        }
        return new VerifyCode(resetPasswordEntity.getUserId());
    }
}
