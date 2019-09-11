package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;
import static org.springframework.http.HttpStatus.*;

public class MiddlewareHttpStatusResolver {
    private static final EnumMap<MiddlewareErrorCode, HttpStatus> container = new EnumMap<>(MiddlewareErrorCode.class);

    private MiddlewareHttpStatusResolver() {
    }

    static {
        //400 Block
        container.put(CURRENCY_MISMATCH, BAD_REQUEST);
        container.put(PAYMENT_PROCESSING_FAILURE, BAD_REQUEST);
        container.put(ACCOUNT_CREATION_VALIDATION_FAILURE, BAD_REQUEST);
        container.put(REQUEST_VALIDATION_FAILURE, BAD_REQUEST);
        container.put(CAN_NOT_RESOLVE_SCA_CHALLENGE_DATA, BAD_REQUEST);

        //403 Block
        container.put(AUTHENTICATION_FAILURE, FORBIDDEN);
        container.put(INSUFFICIENT_PERMISSION, FORBIDDEN);

        //404 Block
        container.put(BRANCH_NOT_FOUND, NOT_FOUND);

        //500 Block
        container.put(NO_SUCH_ALGORITHM, BAD_REQUEST);
    }

    public static HttpStatus resolveHttpStatusByCode(MiddlewareErrorCode code) {
        return container.get(code);
    }
}
