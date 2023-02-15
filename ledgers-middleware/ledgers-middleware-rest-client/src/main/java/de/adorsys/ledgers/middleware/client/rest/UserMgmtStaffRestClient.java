/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.resource.UserMgmtStaffResourceAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ledgersUserMgmtStaff", url = LedgersURL.LEDGERS_URL, path=UserMgmtStaffResourceAPI.BASE_PATH)
public interface UserMgmtStaffRestClient extends UserMgmtStaffResourceAPI {}
