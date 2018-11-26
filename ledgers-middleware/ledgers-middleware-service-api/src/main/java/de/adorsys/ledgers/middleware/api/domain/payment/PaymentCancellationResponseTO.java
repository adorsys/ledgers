package de.adorsys.ledgers.middleware.api.domain.payment;

public class PaymentCancellationResponseTO {
    private boolean cancellationAuthorisationMandated;
    private TransactionStatusTO transactionStatus;

    public PaymentCancellationResponseTO() {
    }

    public PaymentCancellationResponseTO(boolean cancellationAuthorisationMandated, TransactionStatusTO transactionStatus) {
        this.cancellationAuthorisationMandated = cancellationAuthorisationMandated;
        this.transactionStatus = transactionStatus;
    }

    //Getters - Setters
    public boolean isCancellationAuthorisationMandated() {
        return cancellationAuthorisationMandated;
    }

    public void setCancellationAuthorisationMandated(boolean cancellationAuthorisationMandated) {
        this.cancellationAuthorisationMandated = cancellationAuthorisationMandated;
    }

    public TransactionStatusTO getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatusTO transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @Override
    public String toString() {
        return "PaymentCancellationResponseTO{" +
                       "cancellationAuthorisationMandated=" + cancellationAuthorisationMandated +
                       ", transactionStatus=" + transactionStatus +
                       '}';
    }
}
