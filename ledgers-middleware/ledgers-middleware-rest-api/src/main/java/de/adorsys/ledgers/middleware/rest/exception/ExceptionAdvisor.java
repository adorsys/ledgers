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

import de.adorsys.ledgers.deposit.api.exception.DepositModuleException;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.postings.api.exception.PostingModuleException;
import de.adorsys.ledgers.sca.exception.ScaModuleException;
import de.adorsys.ledgers.um.api.exception.UserManagementModuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ExceptionAdvisor {
    private static final String MESSAGE = "message";
    private static final String DEV_MESSAGE = "devMessage";
    private static final String CODE = "code";
    private static final String DATE_TIME = "dateTime";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map> globalExceptionHandler(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, String> body = getHandlerContent(status, null, "Something went wrong during execution of your request.");
        log.error("INTERNAL SERVER ERROR", ex);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map> accessDeniedExceptionHandler(AccessDeniedException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        Map<String, String> body = getHandlerContent(status, null, "Access Denied! You're trying to access resources you have no permission for.");
        log.error("Access Denied: {}", ex.getMessage());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(MiddlewareModuleException.class)
    public ResponseEntity<Map> handleMiddlewareModuleException(MiddlewareModuleException ex) {
        HttpStatus status = MiddlewareHttpStatusResolver.resolveHttpStatusByCode(ex.getErrorCode());
        Map<String, String> body = getHandlerContent(status, null, ex.getDevMsg());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(PostingModuleException.class)
    public ResponseEntity<Map> handlePostingModuleException(PostingModuleException ex) {
        HttpStatus status = PostingHttpStatusResolver.resolveHttpStatusByCode(ex.getErrorCode());
        Map<String, String> body = getHandlerContent(status, null, ex.getDevMsg());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(UserManagementModuleException.class)
    public ResponseEntity<Map> handleUserManagementModuleException(UserManagementModuleException ex) {
        HttpStatus status = UserManagementHttpStatusResolver.resolveHttpStatusByCode(ex.getErrorCode());
        Map<String, String> body = getHandlerContent(status, null, ex.getDevMsg());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(ScaModuleException.class)
    public ResponseEntity<Map> handleScaModuleException(ScaModuleException ex) {
        HttpStatus status = ScaHttpStatusResolver.resolveHttpStatusByCode(ex.getErrorCode());
        Map<String, String> body = getHandlerContent(status, null, ex.getDevMsg());
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(DepositModuleException.class)
    public ResponseEntity<Map> handleDepositModuleException(DepositModuleException ex) {
        HttpStatus status = DepositHttpStatusResolver.resolveHttpStatusByCode(ex.getErrorCode());
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
}
