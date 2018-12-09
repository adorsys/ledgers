package de.adorsys.ledgers.middleware.impl.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.impl.converter.AccessTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.AccessTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
public class MiddlewareOnlineBankingServiceImpl implements MiddlewareOnlineBankingService {
	private static final Logger logger = LoggerFactory.getLogger(MiddlewareOnlineBankingServiceImpl.class);

	private final UserService userService;

	private final UserMapper userTOMapper;
	
	private final AccessTokenMapper accessTokenMapper;

	@Autowired
	public MiddlewareOnlineBankingServiceImpl(UserService userService, UserMapper userTOMapper, AccessTokenMapper accessTokenMapper) {
		this.userService = userService;
		this.userTOMapper = userTOMapper;
		this.accessTokenMapper = accessTokenMapper;
	}

	@Override
	public String authorise(String login, String pin, UserRoleTO role) throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException {
		try {
			UserRoleBO roleBo = UserRoleBO.valueOf(role.name());
			return userService.authorise(login, pin, roleBo);
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		} catch (InsufficientPermissionException e) {
			throw new InsufficientPermissionMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public AccessTokenTO validate(String accessToken) throws UserNotFoundMiddlewareException {
		try {
			AccessTokenBO token = userService.validate(accessToken, new Date());
			return accessTokenMapper.toAccessTokenTO(token);
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO register(String login, String email, String pin, UserRoleTO role) throws UserAlreadyExistsMiddlewareException {
		UserTO user = new UserTO(login, email, pin);
		user.getUserRoles().add(role);
		UserBO userBO = userTOMapper.toUserBO(user);
		try {
			return userTOMapper.toUserTO(userService.create(userBO));
		} catch (UserAlreadyExistsException e) {
			logger.error(e.getMessage(), e);
			throw new UserAlreadyExistsMiddlewareException(user,e);
		}
	}
}
