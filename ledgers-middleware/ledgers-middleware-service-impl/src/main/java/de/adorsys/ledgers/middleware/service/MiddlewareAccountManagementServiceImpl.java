package de.adorsys.ledgers.middleware.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.converter.UserTOMapper;
import de.adorsys.ledgers.middleware.service.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.service.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.service.domain.um.UserTO;
import de.adorsys.ledgers.middleware.service.exception.UserAlreadyExistsMIddlewareException;
import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@Service
public class MiddlewareAccountManagementServiceImpl implements MiddlewareAccountManagementService {

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserTOMapper userTOMapper;
	
	@Override
	public UserTO create(UserTO user) throws UserAlreadyExistsMIddlewareException {
		UserBO userBO = userTOMapper.toUserBO(user);
		try {
			return userTOMapper.toUserTO(userService.create(userBO));
		} catch (UserAlreadyExistsException e) {
			throw new UserAlreadyExistsMIddlewareException(user,e);
		}
	}

	@Override
	public UserTO findById(String id) throws UserNotFoundMiddlewareException {
		try {
			return userTOMapper.toUserTO(userService.findById(id));
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO findByUserLogin(String userLogin) throws UserNotFoundMiddlewareException {
		try {
			return userTOMapper.toUserTO(userService.findByLogin(userLogin));
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO updateScaData(String userLogin, List<ScaUserDataTO> scaDataList)
			throws UserNotFoundMiddlewareException {
		try {
			return userTOMapper.toUserTO(userService.updateScaData(userTOMapper.toScaUserDataListBO(scaDataList), userLogin));
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public UserTO updateAccountAccess(String userLogin, List<AccountAccessTO> accounts)
			throws UserNotFoundMiddlewareException {
		try {
			return userTOMapper.toUserTO(userService.updateAccountAccess(userLogin, userTOMapper.toAccountAccessListBO(accounts)));
		} catch (UserNotFoundException e) {
			throw new UserNotFoundMiddlewareException(e.getMessage(), e);
		}
	}

	@Override
	public List<UserTO> listUsers(int page, int size) {
		return userTOMapper.toUserTOList(userService.listUsers(page, size));
	}

}
