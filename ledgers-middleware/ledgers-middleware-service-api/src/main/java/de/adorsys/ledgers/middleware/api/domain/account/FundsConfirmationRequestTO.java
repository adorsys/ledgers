package de.adorsys.ledgers.middleware.api.domain.account;

import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;

public class FundsConfirmationRequestTO {
    private String psuId;
    private AccountReferenceTO psuAccount;
    private AmountTO instructedAmount;
    private String cardNumber;
    private String payee;

    public FundsConfirmationRequestTO() {
    }

    public FundsConfirmationRequestTO(String psuId, AccountReferenceTO psuAccount, AmountTO instructedAmount, String cardNumber, String payee) {
        this.psuId = psuId;
        this.psuAccount = psuAccount;
        this.instructedAmount = instructedAmount;
        this.cardNumber = cardNumber;
        this.payee = payee;
    }

    //Getters - Setters
    public String getPsuId() {
        return psuId;
    }

    public void setPsuId(String psuId) {
        this.psuId = psuId;
    }

    public AccountReferenceTO getPsuAccount() {
        return psuAccount;
    }

    public void setPsuAccount(AccountReferenceTO psuAccount) {
        this.psuAccount = psuAccount;
    }

    public AmountTO getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(AmountTO instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }
}
