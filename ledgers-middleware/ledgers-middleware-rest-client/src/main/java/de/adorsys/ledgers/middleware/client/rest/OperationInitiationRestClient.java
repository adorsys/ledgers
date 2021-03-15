package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.OperationInitiationRestApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "operationInitiation", url = LedgersURL.LEDGERS_URL, path = OperationInitiationRestApi.BASE_PATH)
public interface OperationInitiationRestClient extends OperationInitiationRestApi {
}
