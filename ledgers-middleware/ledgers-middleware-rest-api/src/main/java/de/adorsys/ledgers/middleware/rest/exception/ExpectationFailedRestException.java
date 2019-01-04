package de.adorsys.ledgers.middleware.rest.exception;

import org.springframework.http.HttpStatus;

public class ExpectationFailedRestException extends RestException {

    public static final String ERROR_CODE = "417_ExpectationFailedException";

    public ExpectationFailedRestException() {
        initialize();
    }

    public ExpectationFailedRestException(String message) {
        super(message);
        initialize();
    }

    private void initialize() {
        withStatus(HttpStatus.EXPECTATION_FAILED);
        withCode(ERROR_CODE);
    }
}
