/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositAccountConfigServiceImpl implements DepositAccountConfigService {
    private final ASPSPConfigData configData;

    @Override
    public String getDepositParentAccount() {
        return configData.getDepositParentAccount();
    }

    @Override
    public String getLedger() {
        return configData.getLedger();
    }

    @Override
    public String getClearingAccount(String paymentProduct) {
        return configData.getClearingAccount(paymentProduct);
    }

    @Override
    public String getCashAccount() {
        return configData.getCashAccount();
    }
}
