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

public interface MiddlewareUserService {
    /**
     * Performs user authorisation
     *
     * @param login  User login
     * @param pin User PIN
     * @return Boolean representation of authorisation status true for success, false for failure or trows a UserNotFoundException
     * @throws UserNotFoundMiddlewareException is thrown if user can`t be found
     */
    boolean authorise(String login, String pin) throws UserNotFoundMiddlewareException;

}
