package de.adorsys.ledgers.middleware.impl.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
public class MiddlewareOnlineBankingServiceImpl implements MiddlewareOnlineBankingService {
	private static final Logger logger = LoggerFactory.getLogger(MiddlewareOnlineBankingServiceImpl.class);

	private final UserService userService;

	private final UserMapper userTOMapper;

	@Autowired
	public MiddlewareOnlineBankingServiceImpl(UserService userService, UserMapper userTOMapper) {
		super();
		this.userService = userService;
		this.userTOMapper = userTOMapper;
	}

	@Override
	public String authorise(String login, String pin) throws UserNotFoundMiddlewareException {
		try {
			return userService.authorise(login, pin);
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO validate(String accessToken) throws UserNotFoundMiddlewareException {
		try {
			UserBO userBO = userService.validate(accessToken, new Date());
			return userTOMapper.toUserTO(userBO);
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO register(String login, String email, String pin) throws UserAlreadyExistsMiddlewareException {
		UserTO user = new UserTO(login, email, pin);
		UserBO userBO = userTOMapper.toUserBO(user);
		try {
			return userTOMapper.toUserTO(userService.create(userBO));
		} catch (UserAlreadyExistsException e) {
			logger.error(e.getMessage(), e);
			throw new UserAlreadyExistsMiddlewareException(user,e);
		}
	}
}
