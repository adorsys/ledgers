/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.rest.exception;

import org.springframework.http.HttpStatus;

public class NotFoundRestException extends RestException {

    public NotFoundRestException() { }

    public NotFoundRestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getCode() {
        return "404_NotFoundRestException";
    }
}
