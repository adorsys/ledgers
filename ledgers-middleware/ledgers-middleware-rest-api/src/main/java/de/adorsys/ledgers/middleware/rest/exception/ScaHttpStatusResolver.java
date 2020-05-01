package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.util.exception.SCAErrorCode;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;

import static de.adorsys.ledgers.util.exception.SCAErrorCode.*;
import static org.springframework.http.HttpStatus.*;

public class ScaHttpStatusResolver {
    private static final EnumMap<SCAErrorCode, HttpStatus> container = new EnumMap<>(SCAErrorCode.class);

    private ScaHttpStatusResolver() {
    }

    static {
        //404 Block
        container.put(USER_SCA_DATA_NOT_FOUND, NOT_FOUND);
        container.put(SCA_OPERATION_NOT_FOUND, NOT_FOUND);

        //400 Block
        container.put(AUTH_CODE_GENERATION_FAILURE, BAD_REQUEST);
        container.put(SCA_OPERATION_VALIDATION_INVALID, BAD_REQUEST);

        //403 Block
        container.put(SCA_OPERATION_USED_OR_STOLEN, FORBIDDEN);
        container.put(SCA_OPERATION_EXPIRED, FORBIDDEN);
        container.put(SCA_VALIDATION_ATTEMPT_FAILED, FORBIDDEN);
        container.put(SCA_OPERATION_FAILED, FORBIDDEN);
        container.put(PSU_AUTH_ATTEMPT_INVALID, FORBIDDEN);
        container.put(AUTHENTICATION_FAILURE, FORBIDDEN);

        //501 Block
        container.put(SCA_METHOD_NOT_SUPPORTED, NOT_IMPLEMENTED);
    }

    public static HttpStatus resolveHttpStatusByCode(SCAErrorCode code) {
        return container.get(code);
    }
}
