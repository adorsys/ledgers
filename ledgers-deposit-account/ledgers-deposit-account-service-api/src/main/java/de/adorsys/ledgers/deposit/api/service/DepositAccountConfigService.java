/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service;

public interface DepositAccountConfigService {

    String getDepositParentAccount();

    String getLedger();

    String getClearingAccount(String paymentProduct);

    String getCashAccount();
}
