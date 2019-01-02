package de.adorsys.ledgers.middleware.rest.mockbank;

import com.google.gson.Gson;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

public class AdminPayload {
	static Gson gson = new Gson();
	
	public static String adminPayload() {
		UserTO admin = new UserTO();
		admin.setEmail("admin@admin.me");
		admin.setLogin("admin");
		admin.setPin("admin123");
		String payload = gson.toJson(admin);
		return payload;
	}
}
