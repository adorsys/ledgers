package de.adorsys.ledgers.mockbank.simple.test;

import com.google.gson.Gson;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

public class AdminPayload {
	static Gson gson = new Gson();

	public static String adminPayload() {
		String payload = gson.toJson(adminUser());
		return payload;
	}
	
	public static UserTO adminUser() {
		UserTO admin = new UserTO();
		admin.setEmail("admin@admin.me");
		admin.setLogin("admin");
		admin.setPin("admin123");
		return admin;
	}
}
