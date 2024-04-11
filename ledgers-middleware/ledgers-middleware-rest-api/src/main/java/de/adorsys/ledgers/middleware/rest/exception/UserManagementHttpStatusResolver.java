/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.util.exception.UserManagementErrorCode;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.*;
import static org.springframework.http.HttpStatus.*;

public class UserManagementHttpStatusResolver {
    private static final EnumMap<UserManagementErrorCode, HttpStatus> container = new EnumMap<>(UserManagementErrorCode.class);

    private UserManagementHttpStatusResolver() {
    }

    static {
        //404 Block
        container.put(USER_NOT_FOUND, NOT_FOUND);
        container.put(CONSENT_NOT_FOUND, NOT_FOUND);
        container.put(TOKEN_NOT_FOUND, NOT_FOUND);

        //400 Block
        container.put(TOKEN_CREATION_ERROR, BAD_REQUEST);
        container.put(RESET_PASSWORD_CODE_SENDING_ERROR, BAD_REQUEST);
        container.put(RESET_PASSWORD_CODE_INVALID, BAD_REQUEST);
        container.put(DUPLICATE_SCA, BAD_REQUEST);
        container.put(INVALID_VERIFICATION_TOKEN, BAD_REQUEST);

        //401 Block
        container.put(INSUFFICIENT_PERMISSION, UNAUTHORIZED);
        container.put(INVALID_CREDENTIAL, UNAUTHORIZED);

        // 403
        container.put(OAUTH_CODE_INVALID, FORBIDDEN);
        container.put(EXPIRED_TOKEN, FORBIDDEN);
        container.put(USER_IS_BLOCKED, FORBIDDEN);
        container.put(USER_IS_TEMPORARY_BLOCKED_BY_SYSTEM, FORBIDDEN);

        //409 Block
        container.put(USER_ALREADY_EXISTS, CONFLICT);
    }

    public static HttpStatus resolveHttpStatusByCode(UserManagementErrorCode code) {
        return container.get(code);
    }
}
