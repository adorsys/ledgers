/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.PaymentRestAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ledgersPayment", url = LedgersURL.LEDGERS_URL, path = PaymentRestAPI.BASE_PATH)
public interface PaymentRestClient extends PaymentRestAPI {
}