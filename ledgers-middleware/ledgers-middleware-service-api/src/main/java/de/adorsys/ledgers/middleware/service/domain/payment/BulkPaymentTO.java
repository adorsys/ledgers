package de.adorsys.ledgers.middleware.service.domain.payment;

import de.adorsys.ledgers.middleware.service.domain.account.AccountReferenceTO;

import java.time.LocalDate;
import java.util.List;

public class BulkPaymentTO {
    private String paymentId;
    private Boolean batchBookingPreferred;
    private AccountReferenceTO debtorAccount;
    private LocalDate requestedExecutionDate;
    private TransactionStatusTO paymentStatus;
    private List<SinglePaymentTO> payments;
    private PaymentProductTO paymentProduct;

    public BulkPaymentTO() {
    }

    public BulkPaymentTO(String paymentId, Boolean batchBookingPreferred, AccountReferenceTO debtorAccount, LocalDate requestedExecutionDate, TransactionStatusTO paymentStatus, List<SinglePaymentTO> payments, PaymentProductTO paymentProduct) {
        this.paymentId = paymentId;
        this.batchBookingPreferred = batchBookingPreferred;
        this.debtorAccount = debtorAccount;
        this.requestedExecutionDate = requestedExecutionDate;
        this.paymentStatus = paymentStatus;
        this.payments = payments;
        this.paymentProduct = paymentProduct;
    }

    //Getters-Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Boolean getBatchBookingPreferred() {
        return batchBookingPreferred;
    }

    public void setBatchBookingPreferred(Boolean batchBookingPreferred) {
        this.batchBookingPreferred = batchBookingPreferred;
    }

    public AccountReferenceTO getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountReferenceTO debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public TransactionStatusTO getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(TransactionStatusTO paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public List<SinglePaymentTO> getPayments() {
        return payments;
    }

    public void setPayments(List<SinglePaymentTO> payments) {
        this.payments = payments;
    }

    public PaymentProductTO getPaymentProduct() {
        return paymentProduct;
    }

    public void setPaymentProduct(PaymentProductTO paymentProduct) {
        this.paymentProduct = paymentProduct;
    }
}
