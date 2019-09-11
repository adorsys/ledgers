package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.ResetDataMgmtStaffAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "resetData", url = LedgersURL.LEDGERS_URL, path = ResetDataMgmtStaffAPI.BASE_PATH)
public interface ResetDataRestClient extends ResetDataMgmtStaffAPI {
}
