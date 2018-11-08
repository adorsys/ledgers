package de.adorsys.ledgers.deposit.api.service.domain;

public class ClearingAccount {
	private String accountNbr;
	private String paymentProduct;
	public String getAccountNbr() {
		return accountNbr;
	}
	public void setAccountNbr(String accountNbr) {
		this.accountNbr = accountNbr;
	}
	public String getPaymentProduct() {
		return paymentProduct;
	}
	public void setPaymentProduct(String paymentProduct) {
		this.paymentProduct = paymentProduct;
	}
}
