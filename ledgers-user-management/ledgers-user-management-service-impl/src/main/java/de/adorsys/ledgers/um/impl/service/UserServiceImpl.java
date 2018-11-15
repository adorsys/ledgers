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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
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
    public UserServiceImpl(UserRepository userRepository,
                           UserConverter userConverter) {
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
    }

    @Override
    public List<UserBO> listUsers(int page, int size) {
        List<UserEntity> content = userRepository.findAll(PageRequest.of(page, size)).getContent();
        return userConverter.toUserBOList(content);
    }

    @Override
    public UserBO findById(String id) throws UserNotFoundException {
        Optional<UserEntity> userPO = userRepository.findById(id);
        userPO.orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, id)));
        return userConverter.toUserBO(userPO.get());
    }

    @Override
	public UserBO findByLogin(String login) throws UserNotFoundException {
    	return userConverter.toUserBO(getUser(login));
	}

    @Override
    public UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) throws UserNotFoundException {
        logger.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));

        List<ScaUserDataEntity> scaMethods = userConverter.toScaUserDataListEntity(scaDataList);
        user.getScaUserData().clear();
        user.getScaUserData().addAll(scaMethods);

        logger.info("{} sca methods would be updated", scaMethods.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
    }

    @Override
    public UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO)
            throws UserNotFoundException {
        logger.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));

        List<AccountAccess> accountAccesses = userConverter.toAccountAccessListEntity(accountAccessListBO);
        user.getAccountAccesses().clear();
        user.getAccountAccesses().addAll(accountAccesses);

        logger.info("{} account accesses would be updated", accountAccesses.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
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
