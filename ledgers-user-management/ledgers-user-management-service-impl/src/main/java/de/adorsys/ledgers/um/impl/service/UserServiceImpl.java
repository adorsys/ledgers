/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.impl.service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserManagementUnexpectedException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.SerializationUtils;

@Service
public class UserServiceImpl implements UserService {
    private static final String USER_WITH_LOGIN_NOT_FOUND = "User with login=%s not found";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id=%s not found";
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final PasswordEnc passwordEnc;

    private final HashMacSecretSource secretSource;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           UserConverter userConverter, 
                           PasswordEnc passwordEnc, HashMacSecretSource secretSource) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.passwordEnc = passwordEnc;
        this.secretSource = secretSource;
    }

    @Override
    public UserBO create(UserBO user) throws UserAlreadyExistsException {
        UserEntity userPO = userConverter.toUserPO(user);
        userPO.setId(Ids.id());
        userPO.setPin(passwordEnc.encode(userPO.getId(),user.getPin()));

        try {
        	return userConverter.toUserBO(userRepository.save(userPO));
        } catch(ConstraintViolationException c) {
        	if(UserEntity.USER_EMAIL_UNIQUE.equals(c.getConstraintName()) ||   //TODO by @speex Let's UserAlreadyExistsException will decide what to do, just pass user and exception args to it
        			UserEntity.USER_LOGIN_UNIQUE.equals(c.getConstraintName())){
        		throw new UserAlreadyExistsException(user, c);
        	} else {
        		throw new UserAlreadyExistsException(c.getMessage(), c);
        	}
        }
    }

    @Override
    public String authorise(String login, String pin) throws UserNotFoundException {
        UserEntity user = getUser(login);
        return authorizeInternal(pin, user);
    }

	@Override
	public UserBO validate(String accessToken, Date refTime) throws UserNotFoundException {
		try {
			SignedJWT jwt = SignedJWT.parse(accessToken);
			JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
			JWSHeader header = jwt.getHeader();
			
			// CHeck algorithm
			if(!JWSAlgorithm.HS256.equals(header.getAlgorithm())) {
				logger.warn("Wrong jws algo for token with subject : " + jwtClaimsSet.getSubject());
				return null;
			}
			
			// CHeck expiration
			if(jwtClaimsSet.getExpirationTime()==null || jwtClaimsSet.getExpirationTime().before(refTime)) {
				logger.warn(String.format("Token with subject %s is expired at %s and reference time is % : " + jwtClaimsSet.getSubject(), jwtClaimsSet.getExpirationTime(), refTime));
				return null;
			}
			
			// check signature.
			boolean verified = jwt.verify(new MACVerifier(secretSource.getHmacSecret()));
			if(!verified) {
				logger.warn("Could not verify signature of token with subject : " + jwtClaimsSet.getSubject());
				return null;
			}
			
			// Retrieve user.
			UserEntity userEntity = userRepository.findById(jwtClaimsSet.getSubject())
				.orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, jwtClaimsSet.getSubject())));

			// Check to make sure all privileges contained in the token are still valid.
			UserEntity userFromToken = SerializationUtils.readValueFromString(jwt.getJWTClaimsSet().toJSONObject(false).toJSONString(), UserEntity.class);
			
			List<AccountAccess> accountAccesses = userEntity.getAccountAccesses();
			List<AccountAccess> accountAccessesFromToken = userFromToken.getAccountAccesses();
			for (AccountAccess accountAccessFT : accountAccessesFromToken) {
				confirmAccess(jwtClaimsSet.getSubject(), accountAccessFT, accountAccesses);
			}

			userEntity.setAccountAccesses(accountAccessesFromToken);;
			return userConverter.toUserBO(userEntity);
			
		} catch (ParseException e) {
			// If we can not parse the token, we log the error and return false.
			logger.warn(e.getMessage());
			return null;
		} catch (JOSEException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
    
	private AccountAccess confirmAccess(String subject, AccountAccess accountAccessFT, List<AccountAccess> accountAccesses) {
		return accountAccesses.stream()
			.filter(a -> matchAccess(accountAccessFT, a))
			.findFirst().orElseGet(() -> {
				logger.warn(String.format("Permission model changed for user with subject %s no sufficient permission on account %s.", subject, accountAccessFT.getIban()));
				return null;
			});
			
	}

	private boolean matchAccess(AccountAccess requested, AccountAccess existent) {
		if(!StringUtils.equals(requested.getIban(), existent.getIban())) {
			return false;
		}
		// Make sure old access still valid
		if(requested.getAccessType().compareTo(existent.getAccessType())>0) {
			return false;
		}
		return false;
	}

	private String authorizeInternal(String pin, UserEntity user) {
		boolean success = passwordEnc.verify(user.getId(), pin, user.getPin());
        if(!success) return null;
        Date iat = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        	.subject(user.getId())
        	.claim("login", user.getLogin())
        	.claim("accountAccesses", user.getAccountAccesses())
        	.claim("scaUserData", user.getScaUserData())
        	.issueTime(iat)
        	.expirationTime(DateUtils.addMinutes(iat, 30)).build();
        
		JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(Ids.id()).build();
		SignedJWT signedJWT = new SignedJWT(header, claimsSet);
		try {
			signedJWT.sign(new MACSigner(secretSource.getHmacSecret()));
		} catch (JOSEException e) {
			throw new UserManagementUnexpectedException("Error signing user token", e);
		}
		return signedJWT.serialize();
	}

    /**
     * If the rationale is knowing if the account belongs toi the user.
     */
    @Override
    public String authorise(String login, String pin, String accountId) throws UserNotFoundException {
        UserEntity user = getUser(login);
        String token = authorizeInternal(pin, user);
        if(token!=null) {
	        List<AccountAccess> accountAccesses = user.getAccountAccesses();
	        for (AccountAccess accountAccess : accountAccesses) {
				if(StringUtils.equals(accountId, accountAccess.getIban())){
					return token;
				}
			}
        }
        return token;
    }

    @Override
    public List<UserBO> listUsers(int page, int size) {
        List<UserEntity> content = userRepository.findAll(PageRequest.of(page, size)).getContent();
        return userConverter.toUserBOList(content);
    }

    @Override
    public UserBO findById(String id) throws UserNotFoundException {
        Optional<UserEntity> userPO = userRepository.findById(id);
        userPO.orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, id)));
        return userConverter.toUserBO(userPO.get());
    }

    @Override
	public UserBO findByLogin(String login) throws UserNotFoundException {
    	return userConverter.toUserBO(getUser(login));
	}

    @Override
    public UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) throws UserNotFoundException {
        logger.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));

        List<ScaUserDataEntity> scaMethods = userConverter.toScaUserDataListEntity(scaDataList);
        user.getScaUserData().clear();
        user.getScaUserData().addAll(scaMethods);

        logger.info("{} sca methods would be updated", scaMethods.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
    }

    @Override
    public UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO)
            throws UserNotFoundException {
        logger.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));

        List<AccountAccess> accountAccesses = userConverter.toAccountAccessListEntity(accountAccessListBO);
        user.getAccountAccesses().clear();
        user.getAccountAccesses().addAll(accountAccesses);

        logger.info("{} account accesses would be updated", accountAccesses.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
    }

    @NotNull
    private UserEntity getUser(String login) throws UserNotFoundException {
        Optional<UserEntity> userOptional = userRepository.findFirstByLogin(login);
        userOptional.orElseThrow(() -> userNotFoundException(login));
        return userOptional.get();
    }

    @NotNull
    private UserNotFoundException userNotFoundException(String login) {
        return new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, login));
    }

}
