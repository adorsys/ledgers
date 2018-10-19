/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.deposit.domain;

public class SinglePayment extends BasePayment {
    private String paymentId;
    private String endToEndIdentification;
    private Amount instructedAmount;
    private AccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private Address creditorAddress;
    private String remittanceInformationUnstructured;
    protected PaymentProduct paymentProduct;
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public String getEndToEndIdentification() {
		return endToEndIdentification;
	}
	public void setEndToEndIdentification(String endToEndIdentification) {
		this.endToEndIdentification = endToEndIdentification;
	}
	public Amount getInstructedAmount() {
		return instructedAmount;
	}
	public void setInstructedAmount(Amount instructedAmount) {
		this.instructedAmount = instructedAmount;
	}
	public AccountReference getCreditorAccount() {
		return creditorAccount;
	}
	public void setCreditorAccount(AccountReference creditorAccount) {
		this.creditorAccount = creditorAccount;
	}
	public String getCreditorAgent() {
		return creditorAgent;
	}
	public void setCreditorAgent(String creditorAgent) {
		this.creditorAgent = creditorAgent;
	}
	public String getCreditorName() {
		return creditorName;
	}
	public void setCreditorName(String creditorName) {
		this.creditorName = creditorName;
	}
	public Address getCreditorAddress() {
		return creditorAddress;
	}
	public void setCreditorAddress(Address creditorAddress) {
		this.creditorAddress = creditorAddress;
	}
	public String getRemittanceInformationUnstructured() {
		return remittanceInformationUnstructured;
	}
	public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
		this.remittanceInformationUnstructured = remittanceInformationUnstructured;
	}
	public PaymentProduct getPaymentProduct() {
		return paymentProduct;
	}
	public void setPaymentProduct(PaymentProduct paymentProduct) {
		this.paymentProduct = paymentProduct;
	}
    
}
