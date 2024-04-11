/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.OauthRestApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "oauthRestClient", url = LedgersURL.LEDGERS_URL, path = OauthRestApi.BASE_PATH)
public interface OauthRestClient extends OauthRestApi {
}
