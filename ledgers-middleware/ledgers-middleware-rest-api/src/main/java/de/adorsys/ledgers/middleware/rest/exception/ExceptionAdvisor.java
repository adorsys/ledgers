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

import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientFundsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
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


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity handleUserNotFoundException(UserNotFoundException e) {
        Map<String, String> body = getHandlerContent(HttpStatus.NOT_FOUND, null, e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientPermissionMiddlewareException.class)
    public ResponseEntity handleInsufficientPermission(InsufficientPermissionMiddlewareException e) {
        Map<String, String> body = getHandlerContent(HttpStatus.FORBIDDEN, null, e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DepositAccountNotFoundException.class)
    public ResponseEntity handleDepositAccountNotFoundException(DepositAccountNotFoundException e) {
        Map<String, String> body = getHandlerContent(HttpStatus.NOT_FOUND, null, e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsMiddlewareException.class)
    public ResponseEntity<SCAPaymentResponseTO> handleInsufficientFundsException(InsufficientFundsMiddlewareException e) {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setTransactionStatus(TransactionStatusTO.RJCT);
        response.setPsuMessage(e.getMessage());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity handleUnsupportedOperationException(UnsupportedOperationException e) {
        return ResponseEntity.status(501).body(e.getMessage());
    }

    @ExceptionHandler(RestException.class)
    public ResponseEntity<Map> handleRestException(RestException ex) {
        Map<String, String> body = getHandlerContent(ex.getCode(), ex.getMessage(), ex.devMessage);
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(PostingModuleException.class)
    public ResponseEntity<Map> handlePostingModuleException(PostingModuleException ex) {
        HttpStatus status = PostingHttpStatusResolver.resolveHttpStatusByCode(ex.getErrorCode());
        Map<String, String> body = getHandlerContent(status, null, ex.getDevMsg());
        return new ResponseEntity<>(body, status);
    }

    //TODO Consider a separate Class for this with a builder?
    private Map<String, String> getHandlerContent(HttpStatus status, String message, String devMessage) {
        Map<String, String> error = new HashMap<>();
        error.put(CODE, String.valueOf(status.value()));
        error.put(MESSAGE, message);
        error.put(DEV_MESSAGE, devMessage);
        error.put(DATE_TIME, LocalDateTime.now().toString());
        return error;
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
