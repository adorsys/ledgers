package de.adorsys.ledgers.deposit.api.exception;

public class DepositAccountAlreadyExistsException extends RuntimeException {
    public DepositAccountAlreadyExistsException(String message) {
        super(message);
    }
}
