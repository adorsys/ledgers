/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.impl.service;

import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.MD5Util;

@Service
public class UserServiceImpl implements UserService {
    private static final String USER_WITH_LOGIN_NOT_FOUND = "User with login=%s not found";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id=%s not found";
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserConverter userConverter;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Override
    public UserBO create(UserBO user) throws UserAlreadyExistsException {
        UserEntity userPO = userConverter.toUserPO(user);

        userPO.setPin(MD5Util.encode(user.getPin()));
        
        try {
        	return userConverter.toUserBO(userRepository.save(userPO));
        } catch(ConstraintViolationException c) {
        	if(UserEntity.USER_EMAIL_UNIQUE.equals(c.getConstraintName()) ||   //TODO by @speex Let's UserAlreadyExistsException will decide what to do, just pass user and exception args to it
        			UserEntity.USER_LOGIN_UNIQUE.equals(c.getConstraintName())){
        		throw new UserAlreadyExistsException(user, c);
        	} else {
        		throw new UserAlreadyExistsException(c.getMessage(), c);
        	}
        }
    }

    @Override
    public boolean authorize(String login, String pin) throws UserNotFoundException {
        UserEntity user = getUser(login);
        return MD5Util.verify(pin, user.getPin());
    }

    /**
     * If the rationale is knowing if the account belongs toi the user.
     */
    @Override
    public boolean authorize(String login, String pin, String accountId) throws UserNotFoundException {
        return authorize(login, pin);
//    	if( authorize(login, pin)) {
        // verify that the user has this account.
        // TODO get Account accesses and check if user is authorized to access this account.
//    		return true;
//    	}
//        UserEntity user = getUser(login);
//        String hashedPin = MD5Util.encode(user.getPin());
//        //        long count = user.getAccounts().stream().filter(a -> a.getId().equals(accountId)).count();
//        //        return pinVerified && count > 0;
//        return MD5Util.verify(pin, hashedPin);
//    	return false;
    }

//    @Override
//    public void addAccount(String login, LedgerAccount account) throws UserNotFoundException {
//        UserPO user = getUser(login);
//        List<LedgerAccount> accounts = user.getAccounts();
//        accounts.add(account);
//        userRepository.save(user);
//    }

    @Override
    public UserBO findById(String id) throws UserNotFoundException {
        Optional<UserEntity> userPO = userRepository.findById(id);
        userPO.orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, id)));
        return userConverter.toUserBO(userPO.get());
    }

    @Override
    public List<ScaUserDataBO> getUserScaData(String userId) throws UserNotFoundException {
        Optional<UserEntity> user = userRepository.findById(userId);
        user.orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, userId)));
        UserBO userBO = userConverter.toUserBO(user.get());
        return userBO.getScaUserData();
    }

    @Override
    public List<AccountAccessBO> getAccountAccess(String userId) throws UserNotFoundException {
        Optional<UserEntity> user = userRepository.findById(userId);
        user.orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, userId)));
        UserBO userBO = userConverter.toUserBO(user.get());
        return userBO.getAccountAccesses();
    }

    @Override
    public List<AccountAccessBO> getAccountAccessByUserLogin(String userLogin) throws UserNotFoundException {
        Optional<UserEntity> user = userRepository.findFirstByLogin(userLogin);
        user.orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));
        UserBO userBO = userConverter.toUserBO(user.get());
        return userBO.getAccountAccesses();
    }

    @Override
    public void updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) throws UserNotFoundException {

        logger.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));

        List<ScaUserDataEntity> scaMethods = userConverter.toScaUserDataListEntity(scaDataList);
        user.setScaUserData(scaMethods);

        logger.info("{} sca methods would be updated", scaMethods.size());
        userRepository.save(user);
    }

    @NotNull
    private UserEntity getUser(String login) throws UserNotFoundException {
        Optional<UserEntity> userOptional = userRepository.findFirstByLogin(login);
        userOptional.orElseThrow(() -> userNotFoundException(login));
        return userOptional.get();
    }

    @NotNull
    private UserNotFoundException userNotFoundException(String login) {
        return new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, login));
    }
}
