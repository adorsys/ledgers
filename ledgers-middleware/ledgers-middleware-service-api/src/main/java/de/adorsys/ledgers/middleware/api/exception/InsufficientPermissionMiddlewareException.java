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

package de.adorsys.ledgers.middleware.api.exception;

/**
 * The connected user does not have enough permission to executed the requested
 * action on the specified account.
 *
 * @author fpo
 */
public class InsufficientPermissionMiddlewareException extends Exception {

    public InsufficientPermissionMiddlewareException() {
    }

    public InsufficientPermissionMiddlewareException(String message) {
        super(message);
    }

    public InsufficientPermissionMiddlewareException(String userId, String userLogin, String requestedIban) {
        super(String.format("Current user with id %s and login %s not owner of the target account with iban %s", userId, userLogin, requestedIban));
    }

    public InsufficientPermissionMiddlewareException(String message, Throwable cause) {
        super(message, cause);
    }
}