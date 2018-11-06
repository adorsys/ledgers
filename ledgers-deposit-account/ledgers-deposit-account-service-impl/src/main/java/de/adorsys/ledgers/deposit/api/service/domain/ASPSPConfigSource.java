package de.adorsys.ledgers.deposit.api.service.domain;

import java.util.List;

public interface ASPSPConfigSource {
	ASPSPConfigData aspspConfigData();
	List<LedgerAccountModel> chartOfAccount(String coaFile);
}
