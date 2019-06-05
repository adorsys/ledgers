package de.adorsys.ledgers.middleware.api.exception;

public class DepositAccountAlreadyExistsMiddlewareException extends RuntimeException {
    public DepositAccountAlreadyExistsMiddlewareException(String message) {
        super(message);
    }
}
