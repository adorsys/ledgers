/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

import org.springframework.cloud.openfeign.FeignClient;

import de.adorsys.ledgers.middleware.rest.resource.AppMgmtRestAPI;

@FeignClient(value = "ledgersAppMgmt", url = LedgersURL.LEDGERS_URL, path=AppMgmtRestAPI.BASE_PATH)
public interface AppMgmtRestClient extends AppMgmtRestAPI {}
