package de.adorsys.ledgers.um.impl.service.password;

import de.adorsys.ledgers.security.UpdatePassword;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.UpdatePasswordService;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.util.PasswordEnc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UpdatePasswordServiceImpl implements UpdatePasswordService {
    private static final String USER_WITH_ID_NOT_FOUND = "User with id=%s not found";

    private final UserRepository userRepository;
    private final PasswordEnc passwordEnc;

    @Override
    @Transactional
    public UpdatePassword updatePassword(String userId, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                                  .orElseThrow(() -> UserManagementModuleException.builder()
                                                             .errorCode(USER_NOT_FOUND)
                                                             .devMsg(String.format(USER_WITH_ID_NOT_FOUND, userId))
                                                             .build());

        user.setPin(passwordEnc.encode(userId, newPassword));
        return new UpdatePassword(true);
    }
}
