package de.adorsys.ledgers.middleware.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.converter.UserMapper;
import de.adorsys.ledgers.middleware.service.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.service.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.service.domain.um.UserTO;
import de.adorsys.ledgers.middleware.service.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
public class MiddlewareUserManagementServiceImpl implements MiddlewareUserManagementService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareUserManagementServiceImpl.class);

	private final UserService userService;
	
	private final UserMapper userTOMapper;
	
	public MiddlewareUserManagementServiceImpl(UserService userService, UserMapper userTOMapper) {
		super();
		this.userService = userService;
		this.userTOMapper = userTOMapper;
	}

	@Override
	public UserTO create(UserTO user) throws UserAlreadyExistsMiddlewareException {
		UserBO userBO = userTOMapper.toUserBO(user);
		try {
			return userTOMapper.toUserTO(userService.create(userBO));
		} catch (UserAlreadyExistsException e) {
			logger.error(e.getMessage(), e);
			throw new UserAlreadyExistsMiddlewareException(user,e);
		}
	}

	@Override
	public UserTO findById(String id) throws UserNotFoundMiddlewareException {
		try {
			return userTOMapper.toUserTO(userService.findById(id));
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO findByUserLogin(String userLogin) throws UserNotFoundMiddlewareException {
		try {
			return userTOMapper.toUserTO(userService.findByLogin(userLogin));
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO updateScaData(String userLogin, List<ScaUserDataTO> scaDataList)
			throws UserNotFoundMiddlewareException {
		try {
			UserBO userBO = userService.updateScaData(userTOMapper.toScaUserDataListBO(scaDataList), userLogin);
			return userTOMapper.toUserTO(userBO);
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO updateAccountAccess(String userLogin, List<AccountAccessTO> accounts)
			throws UserNotFoundMiddlewareException {
		try {
			UserBO userBO = userService.updateAccountAccess(userLogin, userTOMapper.toAccountAccessListBO(accounts));
			return userTOMapper.toUserTO(userBO);
		} catch (UserNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public List<UserTO> listUsers(int page, int size) {
		return userTOMapper.toUserTOList(userService.listUsers(page, size));
	}
	
    @Override
    public boolean authorise(String login, String pin) throws UserNotFoundMiddlewareException {
        try {
            return userService.authorise(login, pin);
        } catch (UserNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new UserNotFoundMiddlewareException(e.getMessage(), e);
        }
    }
}
