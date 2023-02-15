/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.account;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountReportTO {
    private AccountDetailsTO details;
    private List<UserTO> usersAccessingAccount;
    private boolean multilevelScaEnabled;
}
