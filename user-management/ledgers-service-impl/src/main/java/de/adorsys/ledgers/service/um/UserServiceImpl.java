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

package de.adorsys.ledgers.service.um;

import de.adorsys.ledgers.db.um.domain.UserPO;
import de.adorsys.ledgers.db.um.repository.UserRepository;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.service.um.converter.UserConverter;
import de.adorsys.ledgers.service.um.domain.UserBO;
import de.adorsys.ledgers.service.um.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.service.um.exception.UserNotFoundException;
import de.adorsys.ledgers.util.MD5Util;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserConverter userConverter;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserConverter userConverter) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Override
    public UserBO create(UserBO user) throws UserAlreadyExistsException {
        UserPO userPO = userConverter.toUserPO(user);

        if (userRepository.exists(userPO)) {
            throw new UserAlreadyExistsException(user);
        }

        userPO.setPin(MD5Util.encode(user.getPin()));

        return userConverter.toUserBO(userRepository.save(userPO));
    }

    @Override
    public boolean authorize(String login, String pin) throws UserNotFoundException {
        UserPO user = getUser(login);
        return MD5Util.verify(pin, user.getPin());
    }

    @Override
    public boolean authorize(String login, String pin, String accountId) throws UserNotFoundException {
        UserPO user = getUser(login);
        boolean pinVerified = MD5Util.verify(pin, user.getPin());
        long count = user.getAccounts().stream().filter(a -> a.getId().equals(accountId)).count();
        return pinVerified && count > 0;
    }

    @Override
    public void addAccount(String login, LedgerAccount account) throws UserNotFoundException {
        UserPO user = getUser(login);
        List<LedgerAccount> accounts = user.getAccounts();
        accounts.add(account);
        userRepository.save(user);
    }

    @Override
    public UserBO findById(String id) throws UserNotFoundException {
        Optional<UserPO> userPO = userRepository.findById(id);
        userPO.orElseThrow(() -> new UserNotFoundException("User with id=" + id + " was not found"));
        return userConverter.toUserBO(userPO.get());
    }

    @NotNull
    private UserPO getUser(String login) throws UserNotFoundException {
        Optional<UserPO> userOptional = userRepository.findByLogin(login);
        userOptional.orElseThrow(() -> userNotFoundException(login));
        return userOptional.get();
    }

    @NotNull
    private UserNotFoundException userNotFoundException(String login) {
        return new UserNotFoundException(String.format("User with login %s was not found", login));
    }
}
