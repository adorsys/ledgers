package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.DataMgmtStaffAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "dataRestClient", url = LedgersURL.LEDGERS_URL, path = DataMgmtStaffAPI.BASE_PATH)
public interface DataRestClient extends DataMgmtStaffAPI {
}
