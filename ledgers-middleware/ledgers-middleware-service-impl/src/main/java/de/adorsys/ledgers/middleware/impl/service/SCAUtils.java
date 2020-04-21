package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaValidationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.AUTHENTICATION_FAILURE;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.PSU_AUTH_ATTEMPT_INVALID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SCAUtils {
    private final UserService userService;
    private final SCAOperationService scaOperationService;
    private final UserMapper userMapper;

    public ScaUserDataTO getScaMethod(UserTO user, String scaMethodId) {
        if (scaMethodId == null || user.getScaUserData() == null) {
            return null;
        }
        return user.getScaUserData().stream()
                       .filter(uda -> scaMethodId.equals(uda.getId()))
                       .findFirst()
                       .orElse(null);
    }

    public ScaUserDataTO getScaMethod(UserBO user, String scaMethodId) {
        return getScaMethod(userMapper.toUserTO(user), scaMethodId);
    }

    public UserTO user(String userId) {
        return user(userBO(userId));
    }

    public boolean hasSCA(UserBO userBO) {
        return userBO.getScaUserData() != null && !userBO.getScaUserData().isEmpty();
    }

    public UserTO user(UserBO userBO) {
        return userMapper.toUserTO(userBO);
    }

    public UserBO userBO(String userId) {
        return userService.findById(userId);
    }

    public SCAOperationBO loadAuthCode(String authorisationId) {
        return scaOperationService.loadAuthCode(authorisationId);
    }

    public String authorisationId(ScaInfoTO scaInfoTO) {
        String scaId = scaInfoTO.getScaId();
        String authorisationId = scaInfoTO.getAuthorisationId();
        if (Objects.equals(authorisationId, scaId)) {// we are working with login token.
            authorisationId = Ids.id();
        }
        return authorisationId;
    }

    public void checkScaResult(ScaValidationBO scaValidation){
        if (!scaValidation.isValidAuthCode()) {
            String message = scaValidation.getAttemptsLeft() > 0
                                     ? String.format("You have %s attempts to enter valid credentials", scaValidation.getAttemptsLeft())
                                     : "Your Login authorization is FAILED please create a new one.";
            MiddlewareErrorCode code = scaValidation.getAttemptsLeft() > 0
                                               ? PSU_AUTH_ATTEMPT_INVALID
                                               : AUTHENTICATION_FAILURE;
            throw MiddlewareModuleException.builder()
                          .errorCode(code)
                          .devMsg(message)
                          .build();
        }
    }
}
