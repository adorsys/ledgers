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

package de.adorsys.ledgers.um.service.impl;

import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.um.domain.User;
import de.adorsys.ledgers.um.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.exception.UserNotFoundException;
import de.adorsys.ledgers.um.repository.UserRepository;
import de.adorsys.ledgers.um.service.UserService;
import de.adorsys.ledgers.util.MD5Util;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) throws UserAlreadyExistsException {
        String login = user.getLogin();
        String email = user.getEmail();

        if (userRepository.existsByLoginAndEmail(login, email)) {
            throw new UserAlreadyExistsException(String.format("User with login=%s or email=%s already exists", login, email));
        }

        User newUser = User.builder()
                               .id(user.getId())
                               .login(login)
                               .email(email)
                               .pin(MD5Util.encode(user.getPin()))
                               .accounts(user.getAccounts())
                               .build();

        return userRepository.save(newUser);
    }

    @Override
    public boolean authorize(String login, String pin) throws UserNotFoundException {
        User user = getUser(login);
        return MD5Util.verify(pin, user.getPin());
    }

    @Override
    public boolean authorize(String login, String pin, String accountId) throws UserNotFoundException {
        User user = getUser(login);
        boolean pinVerified = MD5Util.verify(pin, user.getPin());
        long count = user.getAccounts().stream().filter(a -> a.getId().equals(accountId)).count();
        return pinVerified && count > 0;
    }

    @Override
    public void addAccount(String login, LedgerAccount account) throws UserNotFoundException {
        User user = getUser(login);
        List<LedgerAccount> accounts = user.getAccounts();
        accounts.add(account);
        userRepository.save(user);
    }

    @NotNull
    private User getUser(String login) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByLogin(login);
        userOptional.orElseThrow(() -> userNotFoundException(login));
        return userOptional.get();
    }

    @NotNull
    private UserNotFoundException userNotFoundException(String login) {
        return new UserNotFoundException(String.format("User with login %s was not found", login));
    }
}
