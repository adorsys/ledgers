/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.rest.exception;

import org.springframework.http.HttpStatus;

public abstract class RestException extends RuntimeException {

    protected HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    protected String devMessage;

    protected RestException() {}

    protected RestException(String message) {
        super(message);
    }

    public String getDevMessage() {
        return devMessage;
    }

    public void setDevMessage(String devMessage) {
        this.devMessage = devMessage;
    }

    public RestException withDevMessage(String devMessage) {
        this.devMessage = devMessage;
        return this;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public RestException withStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public abstract String getCode();
}