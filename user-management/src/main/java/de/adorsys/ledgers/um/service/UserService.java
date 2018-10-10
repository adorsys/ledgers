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

package de.adorsys.ledgers.um.service;

import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.um.domain.User;
import de.adorsys.ledgers.um.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.exception.UserNotFoundException;

public interface UserService {

    User create(User user) throws UserAlreadyExistsException;

    boolean authorize(String id, String pin) throws UserNotFoundException;

    boolean authorize(String id, String pin, String accountId) throws UserNotFoundException;

    void addAccount(String login, LedgerAccount account) throws UserNotFoundException;
}
