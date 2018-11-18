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

package de.adorsys.ledgers.middleware.rest.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public abstract class RestException extends RuntimeException {

    protected HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    protected String code = "500_InternalServerError";

    protected String devMessage;

    protected LocalDateTime dateTime;


    public RestException() {}

    public RestException(String message) {
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

    public RestException withCode(String code) {
        this.code = code;
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

    public String getCode(){
        return code;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}