package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.TransactionsStaffResourceAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "mockTransactionsClient", url = LedgersURL.LEDGERS_URL, path = TransactionsStaffResourceAPI.BASE_PATH)
public interface MockTransactionsStaffRestClient extends TransactionsStaffResourceAPI {
}
