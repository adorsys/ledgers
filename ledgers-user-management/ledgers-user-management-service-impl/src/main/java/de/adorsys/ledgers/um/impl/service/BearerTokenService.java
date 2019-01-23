package de.adorsys.ledgers.um.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;
import de.adorsys.ledgers.um.api.domain.AccessTokenBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.TokenUsageBO;
import de.adorsys.ledgers.um.api.exception.UserManagementUnexpectedException;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import de.adorsys.ledgers.util.Ids;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class BearerTokenService {
    private static final String SCA_ID = "sca_id";
    private static final String AUTHORISATION_ID = "authorisation_id";
	private static final String ACCOUNT_ACCESSES = "account_accesses";
	private static final String CONSENT = "consent";
	private static final String USAGE = "token_usage";
	private static final String ROLE = "role";
	private static final String LOGIN = "login";
	private static final String ACT = "act";
	private static final String MISSING_ROLE = "Missing field for claim role";
	private static final String MISSING_LOGIN = "Missing field for claim login";
	private static final String MISSING_USAGE = "Missing field for claim token_usage";

    private final HashMacSecretSource secretSource;
    private final ObjectMapper objectMapper; 

	public BearerTokenService(HashMacSecretSource secretSource, ObjectMapper objectMapper) {
		this.secretSource = secretSource;
		this.objectMapper = objectMapper;
	}

	public BearerTokenBO bearerToken(String userId, String userLogin, 
			List<AccountAccess> accountAccesses, AisConsentEntity aisConsent,
			UserRole userRole, String scaId, String authorisationId, 
			Date issueTime, Date expires, TokenUsageBO usage, Map<String, String> act) {

        // Generating claim
        JWTClaimsSet claimsSet = genJWT(userId, userLogin, accountAccesses, aisConsent, userRole, scaId, authorisationId, issueTime, expires, usage, act);
        
        AccessTokenBO accessTokenObject = toAccessTokenObject(claimsSet);
        // signing jwt
		String accessTokenString = signJWT(claimsSet);
		
		Long expires_in_seconds = (expires.getTime()-issueTime.getTime())/1000;
		return bearerToken(accessTokenString, expires_in_seconds.intValue(), accessTokenObject);
	}

	private JWTClaimsSet genJWT(String userId, String userLogin, 
			List<AccountAccess> accountAccesses, AisConsentEntity aisConsent,
			UserRole userRole, String scaId, String authorisationId, 
			Date issueTime, Date expires, TokenUsageBO usage, Map<String, String> act) 
	{
		Builder builder = new JWTClaimsSet.Builder()
        	.subject(Objects.requireNonNull(userId, "Missing userId"))
        	.jwtID(Ids.id())
        	.issueTime(issueTime)
        	.expirationTime(expires)
        	.claim(LOGIN, Objects.requireNonNull(userLogin, MISSING_LOGIN))
        	.claim(ROLE, Objects.requireNonNull(userRole, MISSING_ROLE))
			.claim(USAGE, Objects.requireNonNull(usage, MISSING_USAGE).name());

		if(accountAccesses!=null && !accountAccesses.isEmpty()) {
			builder = builder.claim(ACCOUNT_ACCESSES, accountAccesses);
		}
		
		if(StringUtils.isNotBlank(scaId)) {
			builder = builder.claim(SCA_ID, scaId);
		}

		if(StringUtils.isNotBlank(authorisationId)) {
			builder = builder.claim(AUTHORISATION_ID, authorisationId);
		}
		
		if(aisConsent!=null) {
			builder = builder.claim(CONSENT, aisConsent);
		}


		if(act!=null) {
			builder = builder.claim(ACT, act);
		}
		

		return builder.build();
	}

	private String signJWT(JWTClaimsSet claimsSet) {
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(Ids.id()).build();
		SignedJWT signedJWT = new SignedJWT(header, claimsSet);
		try {
			signedJWT.sign(new MACSigner(secretSource.getHmacSecret()));
		} catch (JOSEException e) {
			throw new UserManagementUnexpectedException("Error signing user token", e);
		}
		return signedJWT.serialize();
	}


	public BearerTokenBO bearerToken(String accessToken, int expires_in, AccessTokenBO accessTokenJWT) {
		BearerTokenBO bt = new BearerTokenBO();
		bt.setAccess_token(accessToken);
		bt.setAccessTokenObject(accessTokenJWT);
		bt.setExpires_in(expires_in);
		return bt;
	}

	public int expiresIn(Date refTime, JWTClaimsSet jwtClaimsSet) {
		// CHeck expiration
		Long expireLong = jwtClaimsSet.getExpirationTime()==null
				? -1
				:(jwtClaimsSet.getExpirationTime().getTime()-refTime.getTime())/1000;
		return expireLong.intValue();
	}

	public AccessTokenBO toAccessTokenObject(JWTClaimsSet jwtClaimsSet) {
		// Check to make sure all privileges contained in the token are still valid.
		return objectMapper.convertValue(jwtClaimsSet.toJSONObject(false), AccessTokenBO.class);
	}
	


}
