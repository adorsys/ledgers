package de.adorsys.ledgers.mockbank.simple.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;

@Service
public class UserContextService {

	@Autowired
	private AuthRequestInterceptor auth;
	
	private final Map<String, UserContext> usersMap = new HashMap<>();
	
	public UserContext bagByIbanOrNull(String iban) {
		return usersMap.values().stream().filter(b -> isAccountOwner(b, iban)).findFirst().orElse(null);
	}
	public UserContext bagByIbanOrEx(String iban) {
		return usersMap.values().stream().filter(b -> isAccountOwner(b, iban)).findFirst()
				.orElseThrow(() -> new IllegalStateException(String.format("Owner of this account with iban %s not found.", iban)));
	}
	public UserContext bagByLoginOrNull(String login) {
		return usersMap.get(login);
	}
	public UserContext byLoginOrEx(String login) {
		if(!usersMap.containsKey(login)) {
			throw new IllegalStateException(String.format("User with login %s not in bag.", login));
		}
		return usersMap.get(login);
	}
	private static boolean isAccountOwner(UserContext userBag, String iban) {
		AccountDetailsTO accountDetailsTO = userBag.getAccessibleAccounts().stream()
			.filter(a -> StringUtils.equals(iban, a.getIban())).findFirst().orElse(null);
		if(accountDetailsTO!=null) {
			return true;
		}
		AccountAccessTO accountAccessTO = userBag.getUser().getAccountAccesses().stream()
		.filter(a -> StringUtils.equals(iban, a.getIban())).findFirst().orElse(null);
		
		return accountAccessTO!=null;
	}
	
	public void setContext(String iban) {
		UserContext bag = bagByIbanOrNull(iban);
		if(bag==null) {
			return;
		}
		setContext(bag);
	}
	public void unsetContext() {
		auth.setAccessToken(null);
	}	
	
	public void setContext(UserContext bag) {
		BearerTokenTO accessToken = bag.getAccessToken();
		if(accessToken==null) {
			return;
		}
		auth.setAccessToken(accessToken.getAccess_token());
	}
	public void updateCredentials(String login, UserContext bag) {
		usersMap.put(login, bag);
	}
	public UserContext newToken(BearerTokenTO token) {
		String login = token.getAccessTokenObject().getActor();
		UserContext bag = byLoginOrEx(login);
		bag.setAccessToken(token);
		bag.setRole(token.getAccessTokenObject().getRole());
		return bag;
	}
}
