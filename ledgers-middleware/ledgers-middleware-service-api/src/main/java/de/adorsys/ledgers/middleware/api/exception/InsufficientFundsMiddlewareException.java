package de.adorsys.ledgers.middleware.api.exception;

public class InsufficientFundsMiddlewareException extends RuntimeException {
    public InsufficientFundsMiddlewareException(Throwable cause, String message) {
        super(message,cause);
    }
}
