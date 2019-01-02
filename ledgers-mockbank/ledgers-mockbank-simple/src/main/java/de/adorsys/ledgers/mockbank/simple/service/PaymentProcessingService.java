package de.adorsys.ledgers.mockbank.simple.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.client.rest.AccountRestClient;
import de.adorsys.ledgers.middleware.client.rest.PaymentRestClient;
import de.adorsys.ledgers.mockbank.simple.data.BulkPaymentsData;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.SinglePaymentsData;
import feign.FeignException;

@Service
@SuppressWarnings({"PMD.EmptyIfStmt"})
public class PaymentProcessingService {

	@Autowired	
	private AccountRestClient ledgersAccount;
	@Autowired	
	private PaymentRestClient ledgersPayment;
	@Autowired	
	private UserContextService contextService;
	@Autowired	
	private MockbankInitData sampleData;
	@Autowired
	private DepositAccountService depositAccountService;
	@Autowired
	private AuthCodeReader authCodeReader;

	public void processSinglePayments()throws IOException {
		List<SinglePaymentsData> singlePayments = sampleData.getSinglePayments();
		if(singlePayments==null) {
			return;
		}
		for (SinglePaymentsData singlePaymentData : singlePayments) {
			SinglePaymentTO pymt = singlePaymentData.getSinglePayment();
			// find the debtor iban
			String iban = pymt.getDebtorAccount().getIban();
			execPayment(pymt, iban, PaymentTypeTO.SINGLE, pymt.getEndToEndIdentification(), pymt.getRequestedExecutionDate());
		}
	}
	
	public void processBulkPayments()
			throws IOException {
		List<BulkPaymentsData> bulkPaymentTests = sampleData.getBulkPayments();
		if (bulkPaymentTests == null) {
			return;
		}
		for (BulkPaymentsData bulkPaymentData : bulkPaymentTests) {
			BulkPaymentTO pymt = bulkPaymentData.getBulkPayment();
			// get the end to end id of the firs creditor entry in the list.
			String endToEndIdentification = pymt.getPayments().iterator().next().getEndToEndIdentification();
			// find the debtor iban
			String iban = pymt.getDebtorAccount().getIban();
			execPayment(pymt, iban, PaymentTypeTO.BULK, endToEndIdentification, pymt.getRequestedExecutionDate());
		}
	}

	private void execPayment(Object payment, String iban, PaymentTypeTO pymentType, String end2EndId, LocalDate execDate)
			throws MalformedURLException, IOException, ProtocolException, JsonParseException, JsonMappingException {
		UserContext bag = contextService.bagByIbanOrEx(iban);
		AccountDetailsTO accountDetails = depositAccountService.account(iban)
				.orElseThrow(() -> depositAccountService.numberFormater(iban));
		List<TransactionTO> transactions = transactions(execDate, accountDetails);
		if(!containsPayment(end2EndId, transactions)) {
			SCAPaymentResponseTO response = initiatePymt(bag, payment, pymentType);
			ScaStatusTO scaInitStatus = response.getScaStatus();
			if(ScaStatusTO.EXEMPTED.equals(scaInitStatus)){
				// Execution authorized.
				// Do nothing
			} else if (ScaStatusTO.SCAMETHODSELECTED.equals(scaInitStatus)) {
				// Enter TAN
				authorisePayment(bag, response, authCodeReader.readAuthCode(response.getPaymentId(), response.getAuthorisationId()));
			} else if (ScaStatusTO.PSUIDENTIFIED.equals(scaInitStatus) || ScaStatusTO.PSUAUTHENTICATED.equals(scaInitStatus)) {
				ScaUserDataTO scaMethod = response.getScaMethods().iterator().next();
				// Select TAN
				SCAPaymentResponseTO scaResponse = selectSCA(bag, response, scaMethod.getId());
				ScaStatusTO scaSelectStatus = scaResponse.getScaStatus();
				if (ScaStatusTO.SCAMETHODSELECTED.equals(scaSelectStatus)) {
					// Enter TAN
					authorisePayment(bag, response, authCodeReader.readAuthCode(response.getPaymentId(), response.getAuthorisationId()));
				} else {
					// Failed
					throw new IOException(String.format("Unidentified state after select sca. SacStatus %s. Payment status %s. User massage %s",
							response.getScaStatus(), response.getTransactionStatus(), response.getPsuMessage() ));
				}
			} else {
				// Failed
				throw new IOException(String.format("Unidentified state after init payment. SacStatus %s. Payment status %s. User massage %s",
						response.getScaStatus(), response.getTransactionStatus(), response.getPsuMessage() ));
			}
		}
	}

	/*
	 * Checks if a payment is in the list of transactions.
	 */
	private boolean containsPayment(String endToEndIdentification, List<TransactionTO> transactions) {
		return endToEndIdentification!=null && transactions.stream().filter(t -> endToEndIdentification.equals(t.getEndToEndId())).findFirst().isPresent();
	}

	private List<TransactionTO> transactions(LocalDate execDate, AccountDetailsTO accountDetails) throws IOException {
		try {
			contextService.setContext(accountDetails.getIban());
			ResponseEntity<List<TransactionTO>> res = ledgersAccount.getTransactionByDates(accountDetails.getId(), execDate, LocalDate.now());
			HttpStatus statusCode = res.getStatusCode();
			if (HttpStatus.OK.equals(statusCode)) {
				return res.getBody();
			} else {
				throw new IOException(String.format("Error initiating payment: responseCode %s message %s.",
						res.getStatusCodeValue(), res.getStatusCode()));
			}
		} finally {
			contextService.unsetContext();
		}
	}

	private SCAPaymentResponseTO initiatePymt(UserContext bag, Object payment, PaymentTypeTO paymentTypeTO)
			throws IOException {
		try {
			contextService.setContext(bag);
			ResponseEntity<SCAPaymentResponseTO> res = null;
			HttpStatus statusCode;
			try {
				res = ledgersPayment.initiatePayment(paymentTypeTO, payment);
				statusCode = res.getStatusCode();
			} catch(FeignException f) {
				statusCode = HttpStatus.valueOf(f.status());
			}
			if (HttpStatus.CREATED.equals(statusCode)) {
				return res.getBody();
			} else {
				throw new IOException(String.format("Error initiating payment: responseCode %s message %s.",
						statusCode.value(), statusCode));
			}
		} finally {
			contextService.unsetContext();
		}
	}

	private SCAPaymentResponseTO selectSCA(UserContext bag, SCAPaymentResponseTO paymentResponse, String scaMethodId)
			throws IOException {
		try {
			contextService.setContext(bag);
			ResponseEntity<SCAPaymentResponseTO> res = ledgersPayment.selectMethod(paymentResponse.getPaymentId(), paymentResponse.getAuthorisationId(), scaMethodId);
			HttpStatus statusCode = res.getStatusCode();
			if (HttpStatus.OK.equals(statusCode)) {
				return res.getBody();
			} else {
				throw new IOException(String.format("Error selecting sca method: responseCode %s message %s.",
						res.getStatusCodeValue(), res.getStatusCode()));
			}
		} finally {
			contextService.unsetContext();
		}
	}

	private SCAPaymentResponseTO authorisePayment(UserContext bag, SCAPaymentResponseTO paymentResponse, String authCode)
			throws IOException {
		try {
			contextService.setContext(bag);
			ResponseEntity<SCAPaymentResponseTO> res = ledgersPayment.authorizePayment(paymentResponse.getPaymentId(), paymentResponse.getAuthorisationId(), authCode);
			HttpStatus statusCode = res.getStatusCode();
			if (HttpStatus.OK.equals(statusCode)) {
				return res.getBody();
			} else {
				throw new IOException(String.format("Error authorizing payment: responseCode %s message %s.",
						res.getStatusCodeValue(), res.getStatusCode()));
			}
		} finally {
			contextService.unsetContext();
		}
	}
}
