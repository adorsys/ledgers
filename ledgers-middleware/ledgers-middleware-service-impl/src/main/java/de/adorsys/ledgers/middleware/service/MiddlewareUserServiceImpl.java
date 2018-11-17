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

package de.adorsys.ledgers.middleware.service;

import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MiddlewareUserServiceImpl implements MiddlewareUserService {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareUserServiceImpl.class);

    private final UserService userService;

    public MiddlewareUserServiceImpl(UserService userService) {
        this.userService = userService;
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
