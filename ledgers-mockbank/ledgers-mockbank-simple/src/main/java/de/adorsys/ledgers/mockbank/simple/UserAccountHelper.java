package de.adorsys.ledgers.mockbank.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.resource.AppManagementResource;
import de.adorsys.ledgers.middleware.rest.resource.UserManagementResource;

public class UserAccountHelper {
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String APPLICATION_JSON = "application/json;";
	private static ObjectMapper jsonMapper = new ObjectMapper();
	private static final String UTF_8 = "utf-8";

	private static BearerTokenTO readAccessToken(HttpURLConnection con) throws UnsupportedEncodingException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), UTF_8));
		String accessToken = br.readLine();
		br.close();
		return jsonMapper.readValue(accessToken, BearerTokenTO.class);
	}

	public static BearerTokenTO authorize(String baseUrl, String login, String pin, UserRoleTO role)
			throws UnsupportedEncodingException, IOException, ProtocolException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(UserManagementResource.BASE_PATH)
				.path(UserManagementResource.AUTHORISE_PATH).build().toUri().toURL();

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(UserManagementResource.LOGIN_REQUEST_PARAM, login);
		paramMap.put(UserManagementResource.PIN_REQUEST_PARAM, pin);
		paramMap.put(UserManagementResource.ROLE_REQUEST_PARAM, role.name());

		byte[] content = HTTPParameterStringBuilder.getParamsString(paramMap).getBytes("UTF-8");
		HttpURLConnection con = HttpURLConnectionHelper.postContent(url, null, content, APPLICATION_X_WWW_FORM_URLENCODED);

		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return readAccessToken(con);
		} else if (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
			throw new ForbiddenRestException(String.format("User with login %s has not access with role %s", login,role.name()));
		} else if (con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new NotFoundRestException(String.format("User with login %s not found", login));
		} else {
			throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
					con.getResponseCode(), con.getResponseMessage()));
		}
	}
	
	public static BearerTokenTO authorizeAdmin(String baseUrl) throws IOException {
		UserTO adminUser = AdminPayload.adminUser();
		try {
			return authorize(baseUrl, adminUser.getLogin(), adminUser.getPin(), UserRoleTO.SYSTEM);
		} catch (ForbiddenRestException | NotFoundRestException e) {
			return createAdminAccount(baseUrl);
		}
	}

	private static BearerTokenTO createAdminAccount(String baseUrl) throws IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AppManagementResource.BASE_PATH)
				.path(AppManagementResource.ADMIN_PATH).build().toUri().toURL();
		
		byte[] content = jsonMapper.writeValueAsBytes(AdminPayload.adminUser());
		HttpURLConnection con = HttpURLConnectionHelper.postContent(url, null, content, APPLICATION_JSON);

		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return readAccessToken(con);
		} else {
			throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
					con.getResponseCode(), con.getResponseMessage()));
		}
	}
	
	public static BearerTokenTO authOrCreateCustomer(String baseUrl, UserTO user)
			throws UnsupportedEncodingException, ProtocolException, IOException {
		try {
			return UserAccountHelper.authorize(baseUrl, user.getLogin(), user.getPin(), UserRoleTO.CUSTOMER);
		} catch (ForbiddenRestException | NotFoundRestException e) {
			return registerCustomer(baseUrl, user);
		}
	}

	private static BearerTokenTO registerCustomer(String baseUrl, UserTO user) throws ProtocolException, IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(UserManagementResource.BASE_PATH)
				.path(UserManagementResource.REGISTER_PATH).build().toUri().toURL();

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(UserManagementResource.LOGIN_REQUEST_PARAM, user.getLogin());
		paramMap.put(UserManagementResource.EMAIL_REQUEST_PARAM, user.getEmail());
		paramMap.put(UserManagementResource.PIN_REQUEST_PARAM, user.getPin());

		byte[] content = HTTPParameterStringBuilder.getParamsString(paramMap).getBytes("UTF-8");
		HttpURLConnection con = null;
		try {
			con = HttpURLConnectionHelper.postContent(url, null, content, APPLICATION_X_WWW_FORM_URLENCODED);
	
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// registration worked. Get access token
				try {
					return UserAccountHelper.authorize(baseUrl, user.getLogin(), user.getPin(), UserRoleTO.CUSTOMER);
				} catch (ForbiddenRestException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			} else {
				throw new IOException(String.format("Error registering customer responseCode %s message %s.",
						con.getResponseCode(), con.getResponseMessage()));
			}
		} finally {
			if(con!=null) {
				con.disconnect();
			}
		}
	}
}
