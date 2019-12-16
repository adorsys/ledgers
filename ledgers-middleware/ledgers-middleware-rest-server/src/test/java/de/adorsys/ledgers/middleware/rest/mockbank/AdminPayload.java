package de.adorsys.ledgers.middleware.rest.mockbank;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTypeTO;

public class AdminPayload {
	public static UserTO adminPayload() {
		UserTO admin = new UserTO();
		admin.setEmail("admin@admin.me");
		admin.setLogin("admin");
		admin.setPin("admin123");
		admin.setUserType(UserTypeTO.FAKE);
		return admin;
	}
}
