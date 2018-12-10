package de.adorsys.ledgers.mockbank.simple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.resource.AccountResource;

public class DepositAccountHelper {
	private static final String APPLICATION_JSON = "application/json;";
	private static ObjectMapper jsonMapper = new ObjectMapper();
	private static final TypeReference<ArrayList<AccountDetailsTO>> accountDetailsTOListRef = new TypeReference<ArrayList<AccountDetailsTO>>(){};

	public static void createDepositAccount(String baseUrl, UserTO userTO, UserBag userBag,
			AccountDetailsTO accountDetailsTO) throws JsonProcessingException, MalformedURLException, IOException,
			ProtocolException, UnsupportedEncodingException {
		// Check if account physically exists
		// Create account
		HttpURLConnection con = null;
		try {
			byte[] content = jsonMapper.writeValueAsBytes(accountDetailsTO);
			URL url = UriComponentsBuilder.fromUriString(baseUrl)
					.path(AccountResource.BASE_PATH).build().toUri().toURL();
			con = HttpURLConnectionHelper.postContent(url, userBag.getAccessToken(), content, APPLICATION_JSON);
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK || con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
				// Reauthenticate.
				String accessToken = UserAccountHelper.authorize(baseUrl, userTO.getLogin(), userTO.getPin(), UserRoleTO.CUSTOMER);
				userBag.setAccessToken(accessToken);
			} else {
				throw new IOException(String.format("Error creating account responseCode %s message %s.",
						con.getResponseCode(), con.getResponseMessage()));
			}
		} finally {
			if(con!=null) {
				con.disconnect();
			}
		}
	}

	public static List<AccountDetailsTO> readAccessibleAccounts(String baseUrl, UserBag userBag)
			throws MalformedURLException, IOException, ProtocolException, JsonParseException, JsonMappingException {
		List<AccountDetailsTO> accessibleAccounts = Collections.emptyList();
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AccountResource.BASE_PATH).path(AccountResource.LIST_OF_ACCOUNTS_PATH).build().toUri().toURL();
		HttpURLConnection con = null;
		try {
			con = HttpURLConnectionHelper.getContent(url, userBag.getAccessToken());
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String payload = HttpURLConnectionHelper.readToString(con);
				accessibleAccounts = jsonMapper.readValue(payload, accountDetailsTOListRef);
			} else {
				throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
						con.getResponseCode(), con.getResponseMessage()));
			}
		} finally {
			if(con!=null) {
				con.disconnect();
			}
		}
		return accessibleAccounts;
	}
	
	public static AccountDetailsTO loadAccountDetailsByIban(String baseUrl, String iban, UserBag bag)
			throws MalformedURLException, IOException, ProtocolException, JsonParseException, JsonMappingException {
		// Find account by id
		// Check if account physically exists
		// Create account
		AccountDetailsTO accountDetailsTO = null;
		HttpURLConnection con =  null;
		try {
			URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AccountResource.BASE_PATH)
					.path(AccountResource.IBANS_IBAN_PARAM).build(iban).toURL();
			con = HttpURLConnectionHelper.getContent(url, bag.getAccessToken());
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				accountDetailsTO = jsonMapper.readValue(HttpURLConnectionHelper.readToString(con), AccountDetailsTO.class);
			} else {
				throw new IOException(String.format("Error reading account details responseCode %s message %s.",
						con.getResponseCode(), con.getResponseMessage()));
			}
		} finally {
			if(con!=null) {
				con.disconnect();
			}
		}
		return accountDetailsTO;
	}
	
	public static boolean isAccountOwner(UserBag userBag, String iban) {
		AccountDetailsTO accountDetailsTO = userBag.getAccessibleAccounts().stream()
			.filter(a -> StringUtils.equals(iban, a.getIban())).findFirst().orElse(null);
		if(accountDetailsTO!=null) {
			return true;
		}
		AccountAccessTO accountAccessTO = userBag.getUser().getAccountAccesses().stream()
		.filter(a -> StringUtils.equals(iban, a.getIban())).findFirst().orElse(null);
		
		return accountAccessTO!=null;
	}

	
}