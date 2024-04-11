/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositAccountDetailsBO {
    private DepositAccountBO account;
    private List<BalanceBO> balances = new ArrayList<>();

    public boolean isEnabled() {
        return !account.isBlocked() && !account.isSystemBlocked();
    }
}
