package de.adorsys.ledgers.mockbank.simple.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

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

	@SuppressWarnings({"PMD.CyclomaticComplexity"})
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
			return ledgersUserMgmt.authorizeLogin(scaLoginResponseTO.getScaId(), 
					scaLoginResponseTO.getAuthorisationId(), authCode).getBody().getBearerToken();
		} catch (FeignException f) {
			throw new IOException(String.format("Error authorizing login: responseCode %s message %s.",
					f.status(), f.getMessage()));
		} finally {
			contextService.unsetContext();
		}
	}

	private BearerTokenTO selectSCA(UserContext bag, SCALoginResponseTO scaLoginResponseTO, String scaMethodId) throws IOException {
		try {
			contextService.setContext(bag);
			bag.setAccessToken(scaLoginResponseTO.getBearerToken());
			SCALoginResponseTO scaResponse = ledgersUserMgmt.selectMethod(scaLoginResponseTO.getScaId(), scaLoginResponseTO.getAuthorisationId(), scaMethodId).getBody();
			ScaStatusTO scaSelectStatus = scaResponse.getScaStatus();
			if (ScaStatusTO.SCAMETHODSELECTED.equals(scaSelectStatus)) {
				// Enter TAN
				return authoriseLogin(bag, scaResponse, authCodeReader.readAuthCode(scaResponse.getScaId(), scaResponse.getAuthorisationId()));
			} else {
				// Failed
				throw new IOException(String.format("Unidentified state after select sca. SacStatus %s. User massage %s",
						scaLoginResponseTO.getScaStatus(), scaLoginResponseTO.getPsuMessage() ));
			}
		}catch(FeignException f) {
			throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
					f.status(), f.getMessage()));
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
		try {
			UserTO adminUser = AdminPayload.adminUser();
			contextService.updateCredentials(adminUser.getLogin(), new UserContext(adminUser));
			BearerTokenTO accessToken = ledgersAppMgmt.admin(adminUser).getBody();
			updateBag(adminUser.getLogin(), accessToken);
			return accessToken;
		} catch (FeignException f) {
			HttpStatus statusCode = HttpStatus.valueOf(f.status());
			if (HttpStatus.CONFLICT.equals(statusCode)) {
				throw new ConflictRestException("Admin account exists. No need to create");
			} else {
				throw new IOException(String.format("Error creating admin user responseCode %s message %s.",
						statusCode.value(), statusCode));
			}
		}
	}

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
		try {
			ledgersUserMgmt.register(user.getLogin(), user.getEmail(), user.getPin(), UserRoleTO.CUSTOMER);
			try {
				return authorize(user.getLogin(), user.getPin(), UserRoleTO.CUSTOMER);
			} catch (ForbiddenRestException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		} catch(FeignException f) {
			HttpStatus statusCode = HttpStatus.valueOf(f.status());
			throw new IOException(String.format("Error registering customer responseCode %s message %s.",
						statusCode.value(), statusCode));
		}
	}

	public void updateScaMethods(UserTO userTO) throws IOException {
		try {
			contextService.setContextFromLogin(userTO.getLogin());
			
			UserTO persistent = ledgersUserMgmt.getUser().getBody();
			List<ScaUserDataTO> persistentScaUserData = persistent.getScaUserData();
			List<ScaUserDataTO> scaUserData = userTO.getScaUserData();
			
			// We all all configured to the list of sca to create.
			List<ScaUserDataTO> toCreateScaUserData = new ArrayList<>(scaUserData);
			
			// We collect all unchanged
			List<ScaUserDataTO> unchangedScaUserData = new ArrayList<>();
			
			if(scaUserData!=null) {
				// Anyone that is found in the persistent list:
				scaUserData.forEach(s -> {
					persistentScaUserData.stream()
						.filter(a -> a.getScaMethod().equals(s.getScaMethod()) && 
								a.getMethodValue().equals(s.getMethodValue()))
						.findFirst().ifPresent(u -> {
							// the persistent version is added to the unchanged list.
							unchangedScaUserData.add(u);
							// the transient version is removed from the list of sca to create.
							toCreateScaUserData.remove(s);
						});
				});
			}
			
			unchangedScaUserData.addAll(toCreateScaUserData);
			ledgersUserMgmt.updateUserScaData(unchangedScaUserData);

		}catch(FeignException f) {
			throw new IOException(String.format("Error updating sca data responseCode %s message %s.",
					f.status(), f.getMessage()));
		} finally {
			contextService.unsetContext();
		}
		
	}
}
