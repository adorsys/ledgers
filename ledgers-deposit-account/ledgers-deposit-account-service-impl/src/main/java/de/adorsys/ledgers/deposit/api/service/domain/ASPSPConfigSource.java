/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.domain;

import java.util.List;

public interface ASPSPConfigSource {
	ASPSPConfigData aspspConfigData();
	List<LedgerAccountModel> chartOfAccount(String coaFile);
}
