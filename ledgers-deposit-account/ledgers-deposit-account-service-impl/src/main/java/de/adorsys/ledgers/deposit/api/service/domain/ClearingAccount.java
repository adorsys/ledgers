package de.adorsys.ledgers.deposit.api.service.domain;

import lombok.Data;

@Data
public class ClearingAccount {
	private String accountNbr;
	private String paymentProduct;
}
