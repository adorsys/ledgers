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

import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import org.springframework.stereotype.Service;

//todo: stub should be implemented
@Service
public class StubDepositAccountConfigServiceImpl implements DepositAccountConfigService {
    @Override
    public LedgerAccountBO getDepositParentAccount() {
        return null;
    }

    @Override
    public LedgerBO getLedger() {
        return null;
    }

    @Override
    public LedgerAccountBO getClearingAccount() {
        return null;
    }
}
