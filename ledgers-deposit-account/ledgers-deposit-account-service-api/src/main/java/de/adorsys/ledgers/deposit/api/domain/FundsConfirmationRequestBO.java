package de.adorsys.ledgers.deposit.api.domain;

public class FundsConfirmationRequestBO {
    private String psuId;
    private AccountReferenceBO psuAccount;
    private AmountBO instructedAmount;
    private String cardNumber;
    private String payee;

    public FundsConfirmationRequestBO() {
    }

    public FundsConfirmationRequestBO(String psuId, AccountReferenceBO psuAccount, AmountBO instructedAmount, String cardNumber, String payee) {
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

    public AccountReferenceBO getPsuAccount() {
        return psuAccount;
    }

    public void setPsuAccount(AccountReferenceBO psuAccount) {
        this.psuAccount = psuAccount;
    }

    public AmountBO getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(AmountBO instructedAmount) {
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

    @Override
    public String toString() {
        return "FundsConfirmationRequestBO{" +
                       "psuId='" + psuId + '\'' +
                       ", psuAccount=" + psuAccount +
                       ", instructedAmount=" + instructedAmount +
                       ", cardNumber='" + cardNumber + '\'' +
                       ", payee='" + payee + '\'' +
                       '}';
    }
}
