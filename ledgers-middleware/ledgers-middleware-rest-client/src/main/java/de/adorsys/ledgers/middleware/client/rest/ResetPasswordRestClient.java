package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.ResetPasswordRestAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "resetPassword", url = LedgersURL.LEDGERS_URL, path = ResetPasswordRestAPI.BASE_PATH)
public interface ResetPasswordRestClient extends ResetPasswordRestAPI {
}
