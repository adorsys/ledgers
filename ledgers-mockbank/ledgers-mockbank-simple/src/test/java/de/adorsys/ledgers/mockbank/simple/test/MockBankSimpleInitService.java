package de.adorsys.ledgers.mockbank.simple.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithPrefixGoneMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithSuffixExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.resource.AccountResource;
import de.adorsys.ledgers.middleware.rest.resource.AppManagementResource;
import de.adorsys.ledgers.middleware.rest.resource.PaymentResource;
import de.adorsys.ledgers.middleware.rest.resource.UserManagementResource;
import de.adorsys.ledgers.mockbank.simple.data.BulkPaymentsData;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.SinglePaymentsData;

@Service
public class MockBankSimpleInitService {
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String APPLICATION_JSON = "application/json;";
	private ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
	private ObjectMapper jsonMapper = new ObjectMapper();
	private Gson gson = new Gson();
	private Map<String, UserBag> usersMap = new HashMap<>();

	private final MockbankInitData initData;

	@Autowired
	public MockBankSimpleInitService(MockbankInitData initData) {
		this.initData = initData;
	}

	public void runInit(String baseUrl) throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException,
			UserAlreadyExistsMiddlewareException, AccountWithPrefixGoneMiddlewareException,
			AccountWithSuffixExistsMiddlewareException, UserNotFoundMiddlewareException,
			InsufficientPermissionMiddlewareException, IOException {

		// If !hasAdmin
		String accessToken = authorizeAdmin(baseUrl);
		// if !updateRequired
		initLedgers(baseUrl, accessToken);

		// CHeck if update is required.
		updateIfRequired(baseUrl, initData);

		// Execute single payments
		processSinglePayments(baseUrl, initData);

		// Execute bulk payments
		processBulkPayments(baseUrl, initData);
	}

	private String authorizeAdmin(String baseUrl) throws IOException {
		UserTO adminUser = AdminPayload.adminUser();
		try {
			return authorize(baseUrl, adminUser.getLogin(), adminUser.getPin(), UserRoleTO.SYSTEM);
		} catch (ForbiddenRestException | NotFoundRestException e) {
			return createAdminAccount(baseUrl);
		}
	}

	private String createAdminAccount(String baseUrl) throws IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AppManagementResource.BASE_PATH)
				.path(AppManagementResource.ADMIN_PATH).build().toUri().toURL();

		byte[] content = AdminPayload.adminPayload().getBytes("UTF-8");
		HttpURLConnection con = postContent(url, null, content, APPLICATION_JSON);

		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return readAccessToken(con);
		} else {
			throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
					con.getResponseCode(), con.getResponseMessage()));
		}
	}

	private void initLedgers(String baseUrl, String accessToken) throws IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AppManagementResource.BASE_PATH)
				.path(AppManagementResource.INIT_PATH).build().toUri().toURL();

		HttpURLConnection con = postContent(url, accessToken, new byte[] {}, APPLICATION_JSON);
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
					con.getResponseCode(), con.getResponseMessage()));
		}
	}

	private void processSinglePayments(String baseUrl, MockbankInitData sampleData)
			throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException, ProtocolException, IOException {
		List<SinglePaymentsData> singlePayments = sampleData.getSinglePayments();
		for (SinglePaymentsData singlePayment : singlePayments) {
			// find the debtor iban
			String iban = singlePayment.getSinglePayment().getDebtorAccount().getIban();
			// find the user owner of this account.
			UserBag bag = usersMap.values().stream().filter(b -> isAccountOwner(b, iban)).findFirst().orElse(null);
			if(bag==null) {
				continue;
			}

			SinglePaymentTO pymt = initiatePymt(baseUrl, singlePayment.getSinglePayment(), bag, SinglePaymentTO.class, PaymentTypeTO.SINGLE);
			executePayment(baseUrl, bag, pymt.getPaymentId(), pymt.getPaymentProduct().name(), PaymentTypeTO.SINGLE);
		}
	}

	private void processBulkPayments(String baseUrl, MockbankInitData sampleData)
			throws AccountNotFoundMiddlewareException, PaymentProcessingMiddlewareException, UnsupportedEncodingException, ProtocolException, IOException {
		List<BulkPaymentsData> bulkPaymentTests = sampleData.getBulkPayments();
		if (bulkPaymentTests == null) {
			return;
		}
		for (BulkPaymentsData bulkPayment : bulkPaymentTests) {
			// find the debtor iban
			String iban = bulkPayment.getBulkPayment().getDebtorAccount().getIban();
			// find the user owner of this account.
			UserBag bag = usersMap.values().stream().filter(b -> isAccountOwner(b, iban)).findFirst().orElse(null);
			if(bag==null) {
				continue;
			}
			BulkPaymentTO pymt = initiatePymt(baseUrl, bulkPayment.getBulkPayment(), bag, BulkPaymentTO.class, PaymentTypeTO.BULK);
			executePayment(baseUrl, bag, pymt.getPaymentId(), pymt.getPaymentProduct().name(), PaymentTypeTO.BULK);
		}
	}

	private <T> void executePayment(String baseUrl, UserBag bag, String paymentId, String paymentProduct, PaymentTypeTO paymentTypeTO)
			throws MalformedURLException, IOException, ProtocolException {
		HttpURLConnection con = null;
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(PaymentResource.BASE_PATH)
				.path(PaymentResource.EXECUTE_NO_SCA_PAYMENT_ID__PAYMENT_PRODUCT__PAYMENT_TYPE_PATH)
				.build(paymentId, paymentProduct, paymentTypeTO.name()).toURL();

		try {
			byte[] content = new byte[] {};
			con = postContent(url, bag.getAccessToken(), content, APPLICATION_JSON);
			
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String resultString = readToString(con);
				TransactionStatusTO status = gson.fromJson(resultString, TransactionStatusTO.class);
			} else {
				throw new IOException(String.format("Error initiating payment responseCode %s message %s.",
						con.getResponseCode(), con.getResponseMessage()));
			}
		} finally {
			if(con!=null) {
				con.disconnect();
			}
		}
	}

	private <T> T initiatePymt(String baseUrl, T payment, UserBag bag, Class<T> klass, PaymentTypeTO paymentTypeTO)
			throws UnsupportedEncodingException, IOException, ProtocolException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(PaymentResource.BASE_PATH)
				.path(PaymentResource.PAYMENT_TYPE_PATH_VARIABLE).build(paymentTypeTO.name()).toURL();
		HttpURLConnection con = null;
		T pymt = null;
		try {
//			String c = gson.toJson(payment);
			byte[] content = jsonMapper.writeValueAsBytes(payment);
//			byte[] content = c.getBytes(UTF_8);
			con = postContent(url, bag.getAccessToken(), content, APPLICATION_JSON);
			if (con.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
				String resultString = readToString(con);
				pymt = jsonMapper.readValue(resultString, klass);
			} else {
				throw new IOException(String.format("Error initiating payment responseCode %s message %s.",
						con.getResponseCode(), con.getResponseMessage()));
			}
		} finally {
			if(con!=null) {
				con.disconnect();
			}
		}
		return pymt;
	}

	private boolean isAccountOwner(UserBag userBag, String iban) {
		AccountDetailsTO accountDetailsTO = userBag.getAccessibleAccounts().stream()
			.filter(a -> StringUtils.equals(iban, a.getIban())).findFirst().orElse(null);
		return accountDetailsTO!=null;
	}

	/*
	 * Check if update required. If then process the config file.
	 */
	private void updateIfRequired(String baseUrl, MockbankInitData sampleData)
			throws InsufficientPermissionMiddlewareException, UnsupportedEncodingException, ProtocolException, IOException {
		// No users, not
		if (sampleData.getUsers() == null || sampleData.getUsers().isEmpty()) {
			return;
		}

		List<AccountDetailsTO> accounts = sampleData.getAccounts();
		List<UserTO> users = sampleData.getUsers();
		for (UserTO userTO : users) {
			// Create user
			String accessToken = authOrCreateCustomer(baseUrl, userTO);
			UserBag userBag = new UserBag(userTO, accessToken, UserRoleTO.CUSTOMER);
			List<AccountDetailsTO> accessibleAccounts = readAccessibleAccounts(baseUrl, userBag);
			userBag.getAccessibleAccounts().addAll(accessibleAccounts);
			usersMap.put(userTO.getLogin(), userBag);
			
			List<String> accessibleAccountsFromDBIbans = accessibleAccounts.stream().map(a -> a.getIban()).collect(Collectors.toList());
			List<String> accessibleAccountsFromFileIbans = userTO.getAccountAccesses().stream().map(a -> a.getIban()).collect(Collectors.toList());
			for (AccountDetailsTO accountDetailsTO : accounts) {
				if(!accessibleAccountsFromFileIbans.contains(accountDetailsTO.getIban())) {
					// no assignement of this account to this user in the current files.
					continue;
				}
				
				if(accessibleAccountsFromDBIbans.contains(accountDetailsTO.getIban())) {
					// account already associated with user.
					continue;
				}
				
				// Check if account physically exists
				// Create account
				URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AccountResource.BASE_PATH)
						.queryParam(AccountResource.IBAN_QUERY_PARAM, accountDetailsTO.getIban()).build().toUri().toURL();
				byte[] content = gson.toJson(accountDetailsTO).getBytes(UTF_8);
				HttpURLConnection con = null;
				try {
					con = postContent(url, accessToken, content, APPLICATION_JSON);
					if (con.getResponseCode() == HttpURLConnection.HTTP_OK || con.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
						// Reauthenticate.
						accessToken = authorize(baseUrl, userTO.getLogin(), userTO.getPin(), UserRoleTO.CUSTOMER);
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
		}
	}

	private boolean accountExists(String baseUrl, AccountDetailsTO accountDetailsTO, String accessToken) throws ProtocolException, IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AccountResource.BASE_PATH)
				.queryParam(AccountResource.IBAN_QUERY_PARAM, accountDetailsTO.getIban()).build().toUri().toURL();
		HttpURLConnection con = null;
		try {
			con = getContent(url, accessToken);
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				return true;
			} else if (con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
				return false;
			} else if (con.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
				return true;
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

	private List<AccountDetailsTO> readAccessibleAccounts(String baseUrl, UserBag userBag)
			throws MalformedURLException, IOException, ProtocolException, JsonParseException, JsonMappingException {
		List<AccountDetailsTO> accessibleAccounts = Collections.emptyList();
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(AccountResource.BASE_PATH).build().toUri().toURL();
		HttpURLConnection con = null;
		try {
			con = getContent(url, userBag.getAccessToken());
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String payload = readToString(con);
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

	private String readToString(HttpURLConnection con) throws IOException {
		BufferedReader reader = null;
		StringBuilder stringBuilder;

		// read the output from the server
		reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		stringBuilder = new StringBuilder();

		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		return stringBuilder.toString();
	}

	private String authOrCreateCustomer(String baseUrl, UserTO user)
			throws UnsupportedEncodingException, ProtocolException, IOException {
		try {
			return authorize(baseUrl, user.getLogin(), user.getPin(), UserRoleTO.CUSTOMER);
		} catch (ForbiddenRestException | NotFoundRestException e) {
			return registerCustomer(baseUrl, user);
		}
	}

	private String registerCustomer(String baseUrl, UserTO user) throws ProtocolException, IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(UserManagementResource.BASE_PATH)
				.path(UserManagementResource.REGISTER_PATH).build().toUri().toURL();

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(UserManagementResource.LOGIN_REQUEST_PARAM, user.getLogin());
		paramMap.put(UserManagementResource.EMAIL_REQUEST_PARAM, user.getEmail());
		paramMap.put(UserManagementResource.PIN_REQUEST_PARAM, user.getPin());

		byte[] content = ParameterStringBuilder.getParamsString(paramMap).getBytes("UTF-8");
		HttpURLConnection con = null;
		try {
			con = postContent(url, null, content, APPLICATION_X_WWW_FORM_URLENCODED);
	
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// registration worked. Get access token
				try {
					return authorize(baseUrl, user.getLogin(), user.getPin(), UserRoleTO.CUSTOMER);
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

	private String readAccessToken(HttpURLConnection con) throws UnsupportedEncodingException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), UTF_8));
		String accessToken = br.readLine();
		br.close();
		return accessToken;
	}

	private static final String UTF_8 = "utf-8";

	static class ParameterStringBuilder {

		static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
			StringBuilder result = new StringBuilder();

			for (Map.Entry<String, String> entry : params.entrySet()) {
				result.append(URLEncoder.encode(entry.getKey(), UTF_8));
				result.append("=");
				result.append(URLEncoder.encode(entry.getValue(), UTF_8));
				result.append("&");
			}

			String resultString = result.toString();
			return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
		}
	}

	static class UserBag {
		private UserTO user;
		private String accessToken;
		private UserRoleTO role;
		private List<AccountDetailsTO> accessibleAccounts = new ArrayList<>();

		public UserBag(UserTO user, String accessToken, UserRoleTO role) {
			super();
			this.user = user;
			this.accessToken = accessToken;
			this.role = role;
		}

		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}

		public UserRoleTO getRole() {
			return role;
		}

		public void setRole(UserRoleTO role) {
			this.role = role;
		}

		public UserTO getUser() {
			return user;
		}

		public void setUser(UserTO user) {
			this.user = user;
		}

		public List<AccountDetailsTO> getAccessibleAccounts() {
			return accessibleAccounts;
		}

		public void setAccessibleAccounts(List<AccountDetailsTO> accessibleAccounts) {
			this.accessibleAccounts = accessibleAccounts;
		}
		

	}

	private HttpURLConnection postContent(URL url, String accessToken, byte[] content, String contentType)
			throws IOException, ProtocolException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", contentType);
		con.setRequestProperty("Content-Length", String.valueOf(content.length));
		con.setRequestProperty("Accept", "application/json,text/plain");
		setAuthHeader(accessToken, con);
		
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.write(content);
		out.flush();
//		out.close();		
		return con;
	}

	private HttpURLConnection getContent(URL url, String accessToken) throws IOException, ProtocolException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setDoInput(true);
		con.setRequestProperty("Accept", "application/json,text/plain");
		// Access Token
		setAuthHeader(accessToken, con);
		// give it 15 seconds to respond
		con.setReadTimeout(30 * 1000);
		con.connect();
		return con;
	}

	private void setAuthHeader(String accessToken, HttpURLConnection con) {
		if (accessToken != null) {
			con.setRequestProperty("Authorization", "Bearer " + accessToken);
		}
	}

	private String authorize(String baseUrl, String login, String pin, UserRoleTO role)
			throws UnsupportedEncodingException, IOException, ProtocolException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(UserManagementResource.BASE_PATH)
				.path(UserManagementResource.AUTHORISE2_PATH).build().toUri().toURL();

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put(UserManagementResource.LOGIN_REQUEST_PARAM, login);
		paramMap.put(UserManagementResource.PIN_REQUEST_PARAM, pin);
		paramMap.put(UserManagementResource.ROLE_REQUEST_PARAM, role.name());

		byte[] content = ParameterStringBuilder.getParamsString(paramMap).getBytes("UTF-8");
		HttpURLConnection con = postContent(url, null, content, APPLICATION_X_WWW_FORM_URLENCODED);

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
	
	private static final TypeReference<ArrayList<AccountDetailsTO>> accountDetailsTOListRef = new TypeReference<ArrayList<AccountDetailsTO>>(){};
}