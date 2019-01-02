package de.adorsys.ledgers.mockbank.simple.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AppMgmtRestClient;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtRestClient;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import feign.FeignException;

@Service
public class UserAccountService {
	@Autowired	
	private AppMgmtRestClient ledgersAppMgmt;
	@Autowired	
	private UserMgmtRestClient ledgersUserMgmt;
	@Autowired	
	private UserContextService contextService;
	@Autowired
	private AuthCodeReader authCodeReader;

	@SuppressWarnings({"PMD.IdenticalCatchBranches", "PMD.CyclomaticComplexity"})
	public BearerTokenTO authorize(String login, String pin, UserRoleTO role)
			throws UnsupportedEncodingException, IOException, ProtocolException {

		ResponseEntity<SCALoginResponseTO> res = null;
		HttpStatus statusCode = null;
		try {
			res = ledgersUserMgmt.authorise(login, pin, role);
			statusCode = res.getStatusCode();
		} catch (FeignException f) {
			statusCode = HttpStatus.valueOf(f.status());
		}
		if (res!=null && HttpStatus.OK.equals(statusCode)) {
			SCALoginResponseTO scaLoginResponseTO = res.getBody();
			UserContext bag = updateBag(login, scaLoginResponseTO.getBearerToken());
			ScaStatusTO scaStatus = scaLoginResponseTO.getScaStatus();
			if(ScaStatusTO.EXEMPTED.equals(scaStatus) || ScaStatusTO.PSUAUTHENTICATED.equals(scaStatus)) {
				return scaLoginResponseTO.getBearerToken();
			} else if (ScaStatusTO.PSUIDENTIFIED.equals(scaStatus)) {
				ScaUserDataTO scaMethod = scaLoginResponseTO.getScaMethods().iterator().next();
				// Select TAN
				return selectSCA(bag, scaLoginResponseTO, scaMethod.getId());
			} else if (ScaStatusTO.SCAMETHODSELECTED.equals(scaStatus)) {
				// Enter TAN
				return authoriseLogin(bag, scaLoginResponseTO, authCodeReader.readAuthCode(scaLoginResponseTO.getScaId(), scaLoginResponseTO.getAuthorisationId()));				
			} else {
				// Failed
				throw new IOException(String.format("Unidentified state after select sca. SacStatus %s. User massage %s",
						scaLoginResponseTO.getScaStatus(), scaLoginResponseTO.getPsuMessage() ));
			}
		} else if (HttpStatus.FORBIDDEN.equals(statusCode)) {
			throw new ForbiddenRestException(String.format("User with login %s has not access with role %s", login,role.name()));
		} else if (HttpStatus.NOT_FOUND.equals(statusCode)) {
			throw new NotFoundRestException(String.format("User with login %s not found", login));
		} else {
			throw new IOException(String.format("Error authorizing admin user responseCode %s message %s.",
					res.getStatusCodeValue(), res.getStatusCode()));
		}
	}
	
	private BearerTokenTO authoriseLogin(UserContext bag, SCALoginResponseTO scaLoginResponseTO, String authCode) throws IOException {
		try {
			contextService.setContext(bag);
			bag.setAccessToken(scaLoginResponseTO.getBearerToken());
			ResponseEntity<SCALoginResponseTO> res = null;
			HttpStatus statusCode = null;
			try {
				res = ledgersUserMgmt.authorizeLogin(scaLoginResponseTO.getScaId(), 
						scaLoginResponseTO.getAuthorisationId(), authCode);
				statusCode = res.getStatusCode();
			} catch (FeignException f) {
				statusCode = HttpStatus.valueOf(f.status());
			}
			if (HttpStatus.OK.equals(statusCode)) {
				return res.getBody().getBearerToken();
			} else {
				throw new IOException(String.format("Error authorizing login: responseCode %s message %s.",
						res.getStatusCodeValue(), res.getStatusCode()));
			}
		} finally {
			contextService.unsetContext();
		}
	}

	private BearerTokenTO selectSCA(UserContext bag, SCALoginResponseTO scaLoginResponseTO, String scaMethodId) throws IOException {
		try {
			contextService.setContext(bag);
			bag.setAccessToken(scaLoginResponseTO.getBearerToken());
			ResponseEntity<SCALoginResponseTO> res = ledgersUserMgmt.selectMethod(scaLoginResponseTO.getScaId(), scaLoginResponseTO.getAuthorisationId(), scaMethodId);
			SCALoginResponseTO scaResponse = res.getBody();
			ScaStatusTO scaSelectStatus = scaResponse.getScaStatus();
			if (ScaStatusTO.SCAMETHODSELECTED.equals(scaSelectStatus)) {
				// Enter TAN
				return authoriseLogin(bag, scaResponse, authCodeReader.readAuthCode(scaResponse.getScaId(), scaResponse.getAuthorisationId()));
			} else {
				// Failed
				throw new IOException(String.format("Unidentified state after select sca. SacStatus %s. User massage %s",
						scaLoginResponseTO.getScaStatus(), scaLoginResponseTO.getPsuMessage() ));
			}
		} finally {
			contextService.unsetContext();
		}
	}

	public BearerTokenTO authorizeAdmin() throws IOException {
		UserTO adminUser = AdminPayload.adminUser();
		contextService.updateCredentials(adminUser.getLogin(), new UserContext(adminUser));
		try {
			return authorize(adminUser.getLogin(), adminUser.getPin(), UserRoleTO.SYSTEM);
		} catch (ForbiddenRestException e) {
			throw new IllegalStateException(e);
		}
	}

	public BearerTokenTO createAdminAccount() throws IOException, ConflictRestException {
		UserTO adminUser = AdminPayload.adminUser();
		contextService.updateCredentials(adminUser.getLogin(), new UserContext(adminUser));
		ResponseEntity<BearerTokenTO> res = ledgersAppMgmt.admin(adminUser);
		HttpStatus statusCode = res.getStatusCode();
		if (HttpStatus.OK.equals(statusCode)) {
			BearerTokenTO accessToken = res.getBody();
			updateBag(adminUser.getLogin(), accessToken);
			return accessToken;
		} else if (HttpStatus.CONFLICT.equals(statusCode)) {
			throw new ConflictRestException("Admin account exists. No need to create");
		} else {
			throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
					res.getStatusCodeValue(), res.getStatusCode()));
		}
	}

	@SuppressWarnings("PMD.UnusedFormalParameter")
	private UserContext updateBag(String login, BearerTokenTO accessToken) {
		UserContext bag = contextService.byLoginOrEx(login);
		bag.setAccessToken(accessToken);
		contextService.updateCredentials(login, bag);
		return bag;
	}
	
	public BearerTokenTO authOrCreateCustomer(UserTO user)
			throws UnsupportedEncodingException, ProtocolException, IOException {
		contextService.updateCredentials(user.getLogin(), new UserContext(user));
		try {
			return authorize(user.getLogin(), user.getPin(), UserRoleTO.CUSTOMER);
		} catch (ForbiddenRestException | NotFoundRestException e) {
			return registerCustomer(user);
		}
	}

	private BearerTokenTO registerCustomer(UserTO user) throws ProtocolException, IOException {
		ResponseEntity<UserTO> res = ledgersUserMgmt.register(user.getLogin(), user.getEmail(), user.getPin(), UserRoleTO.CUSTOMER);
		HttpStatus statusCode = res.getStatusCode();
		if (HttpStatus.OK.equals(statusCode)) {
			// registration worked. Get access token
			try {
				return authorize(user.getLogin(), user.getPin(), UserRoleTO.CUSTOMER);
			} catch (ForbiddenRestException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		} else {
			throw new IOException(String.format("Error registering customer responseCode %s message %s.",
					res.getStatusCodeValue(), res.getStatusCode()));
		}
	}
}
