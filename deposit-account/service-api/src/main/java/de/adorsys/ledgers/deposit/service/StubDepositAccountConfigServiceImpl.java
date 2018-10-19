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

package de.adorsys.ledgers.deposit.service;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import org.springframework.stereotype.Service;

//todo: stub should be implemented
@Service
public class StubDepositAccountConfigServiceImpl implements DepositAccountConfigService {
    @Override
    public LedgerAccount getDepositParentAccount() {
        return null;
    }

    @Override
    public Ledger getLedger() {
        return null;
    }

    @Override
    public LedgerAccount getClearingAccount() {
        return null;
    }
}
