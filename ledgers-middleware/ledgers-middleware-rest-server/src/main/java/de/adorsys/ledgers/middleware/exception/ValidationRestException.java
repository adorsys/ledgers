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

package de.adorsys.ledgers.middleware.exception;

import org.springframework.http.HttpStatus;

public class ValidationRestException extends RestException {

    public static final String ERROR_CODE = "422_ValidationRestException";

    public ValidationRestException() {
        initialize();
    }

    public ValidationRestException(String message) {
        super(message);
        initialize();
    }

    private void initialize() {
        withStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        withCode(ERROR_CODE);
    }
}
