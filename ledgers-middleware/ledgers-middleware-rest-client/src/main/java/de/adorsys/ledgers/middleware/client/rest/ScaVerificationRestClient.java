package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.ScaVerificationRestAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ledgersScaVerification", url = LedgersURL.LEDGERS_URL, path = ScaVerificationRestAPI.BASE_PATH)
public interface ScaVerificationRestClient extends ScaVerificationRestAPI {
}
