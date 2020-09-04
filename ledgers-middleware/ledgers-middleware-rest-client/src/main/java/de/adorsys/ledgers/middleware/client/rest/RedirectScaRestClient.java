package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.RedirectScaRestAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ledgersRedirectSca", url = LedgersURL.LEDGERS_URL, path = RedirectScaRestAPI.BASE_PATH)
public interface RedirectScaRestClient extends RedirectScaRestAPI {
}
