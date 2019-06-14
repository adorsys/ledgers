package de.adorsys.ledgers.deposit.api.exception;

public class DepositAccountInsufficientFundsException extends RuntimeException {
    private static final String MESSAGE = "Payment with id=%s rejected due to insufficient funds";

    public DepositAccountInsufficientFundsException(String paymentId) {
        super(String.format(MESSAGE, paymentId));
    }
}
