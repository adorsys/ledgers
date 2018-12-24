package de.adorsys.ledgers.mockbank.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.rest.resource.PaymentResource;
import de.adorsys.ledgers.mockbank.simple.data.BulkPaymentsData;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.SinglePaymentsData;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class PaymentProcessingHelper {
	private static final String APPLICATION_JSON = "application/json;";
	private static ObjectMapper jsonMapper = new ObjectMapper();
	private final Map<String, UserBag> usersMap;
	private final MockbankInitData sampleData;

	public PaymentProcessingHelper(Map<String, UserBag> usersMap, MockbankInitData sampleData) {
		super();
		this.usersMap = usersMap;
		this.sampleData = sampleData;
	}

	void processSinglePayments(String baseUrl)
			throws IOException {
		List<SinglePaymentsData> singlePayments = sampleData.getSinglePayments();
		for (SinglePaymentsData singlePayment : singlePayments) {
			// find the debtor iban
			String iban = singlePayment.getSinglePayment().getDebtorAccount().getIban();
			// find the user owner of this account.
			UserBag bag = usersMap.values().stream().filter(b -> DepositAccountHelper.isAccountOwner(b, iban)).findFirst().orElse(null);
			if(bag==null) {
				continue;
			}

			SinglePaymentTO pymt = initiatePymt(baseUrl, singlePayment.getSinglePayment(), bag, SinglePaymentTO.class, PaymentTypeTO.SINGLE);
			executePayment(baseUrl, bag, pymt.getPaymentId(), pymt.getPaymentProduct().name(), PaymentTypeTO.SINGLE);
		}
	}

	void processBulkPayments(String baseUrl)
			throws IOException {
		List<BulkPaymentsData> bulkPaymentTests = sampleData.getBulkPayments();
		if (bulkPaymentTests == null) {
			return;
		}
		for (BulkPaymentsData bulkPayment : bulkPaymentTests) {
			// find the debtor iban
			String iban = bulkPayment.getBulkPayment().getDebtorAccount().getIban();
			// find the user owner of this account.
			UserBag bag = usersMap.values().stream().filter(b -> DepositAccountHelper.isAccountOwner(b, iban)).findFirst().orElse(null);
			if(bag==null) {
				continue;
			}
			BulkPaymentTO pymt = initiatePymt(baseUrl, bulkPayment.getBulkPayment(), bag, BulkPaymentTO.class, PaymentTypeTO.BULK);
			executePayment(baseUrl, bag, pymt.getPaymentId(), pymt.getPaymentProduct().name(), PaymentTypeTO.BULK);
		}
	}

	private <T> void executePayment(String baseUrl, UserBag bag, String paymentId, String paymentProduct, PaymentTypeTO paymentTypeTO)
			throws IOException {
		HttpURLConnection con = null;
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(PaymentResource.BASE_PATH)
				.path(PaymentResource.EXECUTE_NO_SCA_PATH)
				.build(paymentId, paymentProduct, paymentTypeTO.name()).toURL();

		try {
			byte[] content = new byte[] {};
			con = HttpURLConnectionHelper.postContent(url, bag.getAccessToken(), content, APPLICATION_JSON);
			
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String resultString = HttpURLConnectionHelper.readToString(con);
				jsonMapper.readValue(resultString, TransactionStatusTO.class);
//				TransactionStatusTO status = gson.fromJson(resultString, TransactionStatusTO.class);
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
			throws IOException {
		URL url = UriComponentsBuilder.fromUriString(baseUrl).path(PaymentResource.BASE_PATH)
				.path(PaymentResource.PAYMENT_TYPE_PATH_VARIABLE).build(paymentTypeTO.name()).toURL();
		HttpURLConnection con = null;
		T pymt = null;
		try {
//			String c = gson.toJson(payment);
			byte[] content = jsonMapper.writeValueAsBytes(payment);
//			byte[] content = c.getBytes(UTF_8);
			con = HttpURLConnectionHelper.postContent(url, bag.getAccessToken(), content, APPLICATION_JSON);
			if (con.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
				String resultString = HttpURLConnectionHelper.readToString(con);
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
}
