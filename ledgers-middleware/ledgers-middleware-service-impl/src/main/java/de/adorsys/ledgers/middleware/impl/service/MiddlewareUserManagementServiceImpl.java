package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewareUserManagementServiceImpl implements MiddlewareUserManagementService {
    private final UserService userService;
    private final DepositAccountService depositAccountService;
    private final UserMapper userTOMapper;

    @Override
    public UserTO create(UserTO user) {
        UserBO userBO = userTOMapper.toUserBO(user);
        try {
            return userTOMapper.toUserTO(userService.create(userBO));
        } catch (UserAlreadyExistsException e) {
            throw new UserAlreadyExistsMiddlewareException(user, e);
        }
    }

    @Override
    public UserTO findById(String id) throws UserNotFoundMiddlewareException {
        try {
            return userTOMapper.toUserTO(userService.findById(id));
        } catch (UserNotFoundException e) {
            log.error(e.getMessage());
            throw new UserNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public UserTO findByUserLogin(String userLogin) throws UserNotFoundMiddlewareException {
        try {
            return userTOMapper.toUserTO(userService.findByLogin(userLogin));
        } catch (UserNotFoundException e) {
            log.error(e.getMessage());
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
            log.error(e.getMessage());
            throw new UserNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public UserTO updateAccountAccess(String userLogin, List<AccountAccessTO> accounts)
            throws UserNotFoundMiddlewareException {
        try {
            // check if accounts exist in ledgers deposit account
            for (AccountAccessTO account : accounts) {
                depositAccountService.getDepositAccountByIban(account.getIban(), LocalDateTime.now(), false);
            }

            UserBO userBO = userService.updateAccountAccess(userLogin, userTOMapper.toAccountAccessListBO(accounts));
            return userTOMapper.toUserTO(userBO);
        } catch (UserNotFoundException | DepositAccountNotFoundException e) {
            log.error(e.getMessage());
            throw new UserNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public List<UserTO> listUsers(int page, int size) {
        return userTOMapper.toUserTOList(userService.listUsers(page, size));
    }

    @Override
    public List<UserTO> getUsersByBranchAndRoles(String branch, List<UserRoleTO> roles) {
        List<UserBO> users = userService.findByBranchAndUserRolesIn(branch, userTOMapper.toUserRoleBO(roles));
        return userTOMapper.toUserTOList(users);
    }

    @Override
    public int countUsersByBranch(String branch) {
        return userService.countUsersByBranch(branch);
    }
}
