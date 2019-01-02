package de.adorsys.ledgers.middleware.api.domain.payment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.util.StringUtils;

public class LoginKeyDataTO {
	private static final String SEPARATOR = "#";
	private static final DateTimeFormatter storage_formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final DateTimeFormatter display_formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy");

    private final String userId;
    private final LocalDateTime loginTime;

	public LoginKeyDataTO(String userId, LocalDateTime time) {
		super();
		this.userId = userId;
		this.loginTime = time;
	}

	public String getUserId() {
		return userId;
	}

	public String messageTemplate() {
		if(userId==null) {
			throw new IllegalStateException("Not expecting userId to be null.");
		}

		StringBuilder b = new StringBuilder(String.format("Login process started at %s for %s:\n", loginTime.format(display_formatter), userId));

		b.append("TAN: %s");
		return b.toString();
	}
	
	public String toOpId() {
		return userId + SEPARATOR + loginTime.format(storage_formatter);
	}
	
	public static LoginKeyDataTO fromOpId(String opData) {
		String[] s = StringUtils.split(opData, SEPARATOR);
		return new LoginKeyDataTO(s[0], LocalDateTime.parse(s[1], storage_formatter));
	}
}
