/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.rest.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionAdvisor {
    private static final String MESSAGE = "message";
    private static final String DEV_MESSAGE = "devMessage";
    private static final String CODE = "code";
    private static final String DATE_TIME = "dateTime";

    @ExceptionHandler(RestException.class)
    public ResponseEntity<Map<String, String>> handleRestException(RestException ex) {
        Map<String, String> body = getHandlerContent(ex.getCode(), ex.getMessage(), ex.devMessage);
        return new ResponseEntity<>(body, ex.getStatus());
    }

    private Map<String, String> getHandlerContent(String code, String message, String devMessage) {
        Map<String, String> error = new HashMap<>();
        error.put(CODE, code);
        error.put(MESSAGE, message);
        error.put(DEV_MESSAGE, devMessage);
        error.put(DATE_TIME, LocalDateTime.now().toString());
        return error;
    }
}
