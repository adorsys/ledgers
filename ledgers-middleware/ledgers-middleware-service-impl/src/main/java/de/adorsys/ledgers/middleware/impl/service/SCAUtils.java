package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
		try {
			return userService.findById(userId);
		} catch (UserNotFoundException e) {
			String message = String.format("Can not find user  with id (sub) %s from token", userId);
			log.error(message, e);
			throw new AccountMiddlewareUncheckedException(message, e);
		}
	}

	public SCAOperationBO loadAuthCode(String authorisationId) throws SCAOperationExpiredMiddlewareException {
		try {
			return scaOperationService.loadAuthCode(authorisationId);
		} catch (SCAOperationNotFoundException e) {
			throw new SCAOperationExpiredMiddlewareException(e.getMessage(), e);
		}
	}

	public String authorisationId(ScaInfoTO scaInfoTO) {
		String scaId = scaInfoTO.getScaId();
		String authorisationId = scaInfoTO.getAuthorisationId();
		if(authorisationId.equals(scaId)) {// we are working with login token.
			authorisationId = Ids.id();
		}
		return authorisationId;
	}
}
