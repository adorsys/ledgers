/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.AdminResourceAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ledgersAdmin", url = LedgersURL.LEDGERS_URL, path = AdminResourceAPI.BASE_PATH)
public interface AdminRestClient extends AdminResourceAPI {
}
