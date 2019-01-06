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

package de.adorsys.ledgers.deposit.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigData;

@Service
public class DepositAccountConfigServiceImpl implements DepositAccountConfigService {
	
	private final ASPSPConfigData configData;

    @Autowired
    public DepositAccountConfigServiceImpl(ASPSPConfigData configData) {
        this.configData = configData;
    }
	
	@Override
	public String getDepositParentAccount() {
    	return configData.getDepositParentAccount();
	}

	@Override
	public String getLedger() {
    	return configData.getLedger();
	}

	@Override
	public String getClearingAccount(PaymentProductBO paymentProduct) {
		return configData.getClearingAccount(paymentProduct.name());
	}

	@Override
	public String getCashAccount() {
		return configData.getCashAccount();
	}
}
