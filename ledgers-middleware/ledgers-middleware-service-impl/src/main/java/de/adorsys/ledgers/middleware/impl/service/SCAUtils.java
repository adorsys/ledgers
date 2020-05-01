package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
}
