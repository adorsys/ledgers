package de.adorsys.ledgers.um.impl.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;

import de.adorsys.ledgers.um.api.domain.AccessTokenBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.exception.UserManagementUnexpectedException;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.UserRole;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.SerializationUtils;

@Service
public class BearerTokenService {
    private static final String SCA_ID = "scaId";
	private static final String ACCOUNT_ACCESSES = "accountAccesses";
	private static final String ROLE = "role";
	private static final String ACTOR = "actor";

    private final HashMacSecretSource secretSource;
	public BearerTokenService(HashMacSecretSource secretSource) {
		super();
		this.secretSource = secretSource;
	}

	public JWTClaimsSet genJWT(String userId, String userLogin, List<AccountAccess> accountAccesses, UserRole userRole, String scaId, Date issueTime, int validitySeconds) {
		Builder builder = new JWTClaimsSet.Builder()
        	.subject(userId)
        	.jwtID(Ids.id())
        	.claim(ACTOR, userLogin)
        	.claim(ROLE, userRole)
        	.issueTime(issueTime)
        	.expirationTime(DateUtils.addSeconds(issueTime, validitySeconds));
		
		if(accountAccesses!=null && !accountAccesses.isEmpty()) {
			builder = builder.claim(ACCOUNT_ACCESSES, accountAccesses);
		}
		
		if(StringUtils.isNotBlank(scaId)) {
			builder = builder.claim(SCA_ID, scaId);
		}
		return builder.build();
	}

	public String signJWT(JWTClaimsSet claimsSet) {
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
				:jwtClaimsSet.getExpirationTime().getTime()-refTime.getTime();
		return expireLong.intValue();
	}

	public AccessTokenBO toAccessTokenObject(JWTClaimsSet jwtClaimsSet) {
		// Check to make sure all privileges contained in the token are still valid.
		return SerializationUtils.readValueFromString(jwtClaimsSet.toJSONObject(false).toJSONString(), AccessTokenBO.class);
	}
    

}
