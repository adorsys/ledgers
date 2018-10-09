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
import de.adorsys.ledgers.um.repository.UserRepository;
import de.adorsys.ledgers.um.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) throws UserAlreadyExistsException {
        String email = user.getEmail();
        if (userRepository.existsById(email)) {
            throw new UserAlreadyExistsException(String.format("User with email %s already exists", email));
        }
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return null;
    }

    @Override
    public void addAccount(String userEmail, LedgerAccount account) {

    }
}
