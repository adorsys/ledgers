/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.mock;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BalancesData {
	private List<AccountBalances> balancesList = new ArrayList<>();
}
