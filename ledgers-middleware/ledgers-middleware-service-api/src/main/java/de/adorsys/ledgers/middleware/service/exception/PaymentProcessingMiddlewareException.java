package de.adorsys.ledgers.middleware.service.exception;

public class PaymentProcessingMiddlewareException extends Exception {
    private static final long serialVersionUID = -1713219984198663520L;

    public PaymentProcessingMiddlewareException(String message) {
        super(message);
    }

    public PaymentProcessingMiddlewareException(String paymentId, Throwable e) {
        this(String.format("Payment id %s execution failed due to: %s.", paymentId, e.getMessage()));
    }

}
