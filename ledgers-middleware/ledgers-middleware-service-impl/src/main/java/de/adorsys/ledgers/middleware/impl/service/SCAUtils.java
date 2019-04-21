package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SCAUtils {
	private static final Logger logger = LoggerFactory.getLogger(MiddlewarePaymentServiceImpl.class);
	private final UserService userService;
	private final SCAOperationService scaOperationService;
	private final UserMapper userMapper;
	private final AccessTokenTO accessTokenTO;

	public SCAUtils(UserService userService, SCAOperationService scaOperationService, UserMapper userMapper,
			AccessTokenTO accessTokenTO) {
		this.userService = userService;
		this.scaOperationService = scaOperationService;
		this.userMapper = userMapper;
		this.accessTokenTO = accessTokenTO;
	}

	public ScaUserDataTO getScaMethod(UserTO user, String scaMethodId) {
		if (scaMethodId == null || user.getScaUserData() == null) {
			return null;
		}
		return user.getScaUserData().stream()
				       .filter(uda -> scaMethodId.equals(uda.getId()))
				       .findFirst()
				       .orElse(null);
	}

	public UserTO user() {
		return user(userBO());
	}
	
	public boolean hasSCA(UserBO userBO) {
		return userBO.getScaUserData() != null && !userBO.getScaUserData().isEmpty();
	}

	public UserTO user(UserBO userBO) {
		return userMapper.toUserTO(userBO);
	}

	public UserBO userBO() {
		try {
			return userService.findById(accessTokenTO.getSub());
		} catch (UserNotFoundException e) {
			String message = String.format("Can not find user  with id (sub) %s from token", accessTokenTO.getSub());
			logger.error(message, e);
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
	
	public UserRoleTO getCurrentRole() {
		return accessTokenTO.getRole();
	}
	
	public String authorisationId() {
		String scaId = accessTokenTO.getScaId();
		String authorisationId = accessTokenTO.getAuthorisationId();
		if(authorisationId.equals(scaId)) {// we are working with login token.
			authorisationId = Ids.id(); 
		}
		return authorisationId;
	}

	
}
