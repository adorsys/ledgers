/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.mockbank;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

public class AdminPayload {
	public static UserTO adminPayload() {
		UserTO admin = new UserTO();
		admin.setEmail("admin@admin.me");
		admin.setLogin("admin");
		admin.setPin("admin");
		return admin;
	}
}
