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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.exception.*;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.*;
import de.adorsys.ledgers.um.db.repository.AisConsentRepository;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.AisConsentMapper;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.PasswordEnc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("PMD.TooManyMethods") // TODO: refactor class: issue #228
public class UserServiceImpl implements UserService {

    private static final String NO_TRANSACTION_ACCESS_USER_DOES_NOT_HAVE_ACCESS = "No transaction access. User with id %s does not have access to accounts %s";
    private static final String NO_BALANCE_ACCESS_DOES_NOT_HAVE_ACCESS = "No balance access. User with id %s does not have access to accounts %s";
    private static final String NO_ACCOUNT_ACCESS_DOES_NOT_HAVE_ACCESS = "No account access. User with id %s does not have access to accounts %s";
    private static final String CAN_NOT_LOAD_USER_WITH_ID = "Can not load user with id: ";
    private static final String PERMISSION_MODEL_CHANGED_NO_SUFFICIENT_PERMISSION = "Permission model changed for user with subject %s no sufficient permission on account %s.";
    private static final String COULD_NOT_VERIFY_SIGNATURE_OF_TOKEN_WITH_SUBJECT = "Could not verify signature of token with subject : {}";
    private static final String TOKEN_WITH_SUBJECT_EXPIRED = "Token with subject {} is expired at {} and reference time is {}";
    private static final String WRONG_JWS_ALGO_FOR_TOKEN_WITH_SUBJECT = "Wrong jws algo for token with subject : {}";
    private static final String USER_DOES_NOT_HAVE_THE_ROLE_S = "User with id %s and login %s does not have the role %s";
    private static final String USER_WITH_LOGIN_NOT_FOUND = "User with login=%s not found";
    private static final String USER_WITH_ID_NOT_FOUND = "User with id=%s not found";
    private static final String CONSENT_WITH_ID_S_NOT_FOUND = "Consent with id=%s not found";
    private static final int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.

    private final UserRepository userRepository;
    private final AisConsentRepository consentRepository;
    private final UserConverter userConverter;
    private final PasswordEnc passwordEnc;
    private final HashMacSecretSource secretSource;
    private final AisConsentMapper aisConsentMapper;
    private final BearerTokenService bearerTokenService;

    @Override
    public UserBO create(UserBO user) {
        checkUserAlreadyExists(user);

        UserEntity userPO = userConverter.toUserPO(user);

        // if user is TPP and has an ID than do not reset it
        if (userPO.getId() == null) {
            log.info("User with login {} has no id, generating one", userPO.getLogin());
            userPO.setId(Ids.id());
        }

        userPO.setPin(passwordEnc.encode(userPO.getId(), user.getPin()));
        return userConverter.toUserBO(userRepository.save(userPO));
    }

    @Override
    public BearerTokenBO authorise(String login, String pin, UserRoleBO role, String scaId, String authorisationId) throws InsufficientPermissionException {
        UserEntity user = getUser(login);
        boolean success = passwordEnc.verify(user.getId(), pin, user.getPin());
        if (!success) {
            return null;
        }

        // Check user has defined role.
        UserRole userRole = user.getUserRoles().stream().filter(r -> r.name().equals(role.name()))
                                    .findFirst().orElseThrow(() -> new InsufficientPermissionException(String.format(USER_DOES_NOT_HAVE_THE_ROLE_S, user.getId(), user.getLogin(), role)));

        String scaIdParam = scaId != null
                                    ? scaId
                                    : Ids.id();
        String authorisationIdParam = authorisationId != null
                                              ? authorisationId
                                              : scaIdParam;

        Date issueTime = new Date();
        Date expires = DateUtils.addSeconds(issueTime, defaultLoginTokenExpireInSeconds);
        return bearerTokenService.bearerToken(user.getId(), user.getLogin(),
                                              null, null, userRole, scaIdParam, authorisationIdParam, issueTime, expires, TokenUsageBO.LOGIN, null);
    }

    @Override
    public BearerTokenBO validate(String accessToken, Date refTime) throws InsufficientPermissionException {
        try {
            SignedJWT jwt = SignedJWT.parse(accessToken);
            JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
            JWSHeader header = jwt.getHeader();

            // CHeck algorithm
            if (!JWSAlgorithm.HS256.equals(header.getAlgorithm())) {
                log.warn(WRONG_JWS_ALGO_FOR_TOKEN_WITH_SUBJECT, jwtClaimsSet.getSubject());
                return null;
            }

            int expiresIn = bearerTokenService.expiresIn(refTime, jwtClaimsSet);

            if (expiresIn <= 0) {
                log.warn(TOKEN_WITH_SUBJECT_EXPIRED, jwtClaimsSet.getSubject(), jwtClaimsSet.getExpirationTime(), refTime);
                return null;
            }

            // check signature.
            boolean verified = jwt.verify(new MACVerifier(secretSource.getHmacSecret()));
            if (!verified) {
                log.warn(COULD_NOT_VERIFY_SIGNATURE_OF_TOKEN_WITH_SUBJECT, jwtClaimsSet.getSubject());
                return null;
            }

            // Retrieve user.
            UserEntity userEntity = userRepository.findById(jwtClaimsSet.getSubject())
                                            .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, jwtClaimsSet.getSubject())));

            AccessTokenBO accessTokenJWT = bearerTokenService.toAccessTokenObject(jwtClaimsSet);

            validateAccountAcesses(userEntity, accessTokenJWT);
            UserBO user = userConverter.toUserBO(userEntity);
            AisConsentBO aisConsent = accessTokenJWT.getConsent();
            validateAisConsent(aisConsent, user);

            return bearerTokenService.bearerToken(accessToken, expiresIn, accessTokenJWT);

        } catch (ParseException | JOSEException e) {
            // If we can not parse the token, we log the error and return false.
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<UserBO> listUsers(int page, int size) {
        List<UserEntity> content = userRepository.findAll(PageRequest.of(page, size)).getContent();
        return userConverter.toUserBOList(content);
    }

    @Override
    public UserBO findById(String id) {
        UserEntity userPO = userRepository.findById(id)
                                    .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_ID_NOT_FOUND, id)));
        return userConverter.toUserBO(userPO);
    }

    @Override
    public UserBO findByLogin(String login) {
        return userConverter.toUserBO(getUser(login));
    }

    @Override
    public UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) {
        log.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));

        List<ScaUserDataEntity> scaMethods = userConverter.toScaUserDataListEntity(scaDataList);
        user.getScaUserData().clear();
        user.getScaUserData().addAll(scaMethods);

        log.info("{} sca methods would be updated", scaMethods.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
    }

    @Override
    public UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO) {
        log.info("Retrieving user by login={}", userLogin);
        UserEntity user = userRepository.findFirstByLogin(userLogin)
                                  .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, userLogin)));

        List<AccountAccess> accountAccesses = userConverter.toAccountAccessListEntity(accountAccessListBO);
        user.getAccountAccesses().clear();
        user.getAccountAccesses().addAll(accountAccesses);

        log.info("{} account accesses would be updated", accountAccesses.size());
        UserEntity save = userRepository.save(user);
        return userConverter.toUserBO(save);
    }

    @Override
    public BearerTokenBO consentToken(AccessTokenBO loginToken, AisConsentBO aisConsent) throws InsufficientPermissionException {
        UserBO user;
        try {
            user = findById(loginToken.getSub());
        } catch (UserNotFoundException e) {
            throw new UserManagementUnexpectedException(e);
        }
        aisConsent.setUserId(user.getId());

        validateAisConsent(aisConsent, user);

        Date issueTime = new Date();
        Date expires = getExpirationDate(aisConsent, issueTime);
        // Produce the token
        Map<String, String> act = new HashMap<>();
        String tppId = aisConsent.getTppId();
        act.put("tppId", tppId);
        UserRole userRole = UserRole.valueOf(loginToken.getRole().name());
        return bearerTokenService.bearerToken(user.getId(), user.getLogin(), null,
                                              aisConsent, userRole,
                                              loginToken.getScaId(), loginToken.getAuthorisationId(), issueTime, expires, TokenUsageBO.DELEGATED_ACCESS, act);
    }

    @Override
    public BearerTokenBO scaToken(AccessTokenBO loginToken) {
        return getToken(loginToken, loginToken.getAuthorisationId(), TokenUsageBO.DIRECT_ACCESS);
    }

    @Override
    public BearerTokenBO loginToken(AccessTokenBO loginToken, String authorisationId) {
        return getToken(loginToken, authorisationId, TokenUsageBO.LOGIN);
    }

    @Override
    public AisConsentBO storeConsent(AisConsentBO consentBO) {
        AisConsentEntity consentEntity = consentRepository.findById(consentBO.getId()).orElse(consentRepository.save(aisConsentMapper.toAisConsentPO(consentBO)));
        return aisConsentMapper.toAisConsentBO(consentEntity);
    }

    @Override
    public AisConsentBO loadConsent(String consentId) throws ConsentNotFoundException {
        AisConsentEntity aisConsentEntity = consentRepository.findById(consentId)
                                                    .orElseThrow(() -> new ConsentNotFoundException(String.format(CONSENT_WITH_ID_S_NOT_FOUND, consentId)));
        return aisConsentMapper.toAisConsentBO(aisConsentEntity);
    }

    @Override
    public List<UserBO> findByBranchAndUserRolesIn(String branch, List<UserRoleBO> userRoles) {
        List<UserEntity> userEntities = userRepository.findByBranchAndUserRolesIn(branch, userConverter.toUserRole(userRoles));
        return userConverter.toUserBOList(userEntities);
    }

    @Override
    public int countUsersByBranch(String branch) {
        return userRepository.countByBranch(branch);
    }

    /**
     * Makes sure the user has access to all those accounts.
     *
     * @param accessibleAccounts accessible account
     * @param requestedAccounts  requested accounts
     * @param message            message
     * @throws InsufficientPermissionException exception thrown
     */
    private void checkAccountAccess(List<String> accessibleAccounts, List<String> requestedAccounts, String message, String userId) throws InsufficientPermissionException {
        ArrayList<String> copy = new ArrayList<>();
        if (requestedAccounts != null) {
            copy.addAll(requestedAccounts);
        }

        copy.removeAll(accessibleAccounts);

        if (!copy.isEmpty()) {
            throw new InsufficientPermissionException(String.format(message, userId, copy.toString()));
        }
    }

    private void validateAisConsent(AisConsentBO aisConsent, UserBO user) throws InsufficientPermissionException {
        if (aisConsent == null) {
            return;
        }

        List<String> accessibleAccounts = user.getAccountAccesses().stream()
                                                  .map(AccountAccessBO::getIban)
                                                  .collect(Collectors.toList());

        AisAccountAccessInfoBO access = aisConsent.getAccess();
        if (access != null) {
            checkAccountAccess(accessibleAccounts, access.getAccounts(), NO_ACCOUNT_ACCESS_DOES_NOT_HAVE_ACCESS, user.getId());
            checkAccountAccess(accessibleAccounts, access.getBalances(), NO_BALANCE_ACCESS_DOES_NOT_HAVE_ACCESS, user.getId());
            checkAccountAccess(accessibleAccounts, access.getTransactions(), NO_TRANSACTION_ACCESS_USER_DOES_NOT_HAVE_ACCESS, user.getId());
        }
    }

    private Date getExpirationDate(AisConsentBO aisConsent, Date iat) {
        LocalDate expirationLocalDate = aisConsent.getValidUntil();
        return expirationLocalDate == null
                       ? DateUtils.addDays(iat, 90) // default to 90 days
                       : Date.from(expirationLocalDate.atTime(23, 59, 59, 99).atZone(ZoneId.systemDefault()).toInstant());
    }

    @NotNull
    private UserEntity getUser(String login) {
        return userRepository.findFirstByLogin(login)
                       .orElseThrow(() -> new UserNotFoundException(String.format(USER_WITH_LOGIN_NOT_FOUND, login)));
    }

    private void checkUserAlreadyExists(UserBO userBO) {
        Optional<UserEntity> user = userRepository.findByEmailOrLogin(userBO.getEmail(), userBO.getLogin());
        if (user.isPresent()) {
            String message = String.format("User with this email or login already exists. Email %s. Login %s.",
                                           userBO.getEmail(), userBO.getLogin());
            log.error(message);
            throw new UserAlreadyExistsException(message);
        }
    }

    private void validateAccountAcesses(UserEntity userEntity, AccessTokenBO accessTokenJWT) throws InsufficientPermissionException {
        List<AccountAccess> accountAccessesFromToken = userConverter.toAccountAccessListEntity(accessTokenJWT.getAccountAccesses());
        for (AccountAccess accountAccessFT : accountAccessesFromToken) {
            confirmAndReturnAccess(userEntity.getId(), accountAccessFT, userEntity.getAccountAccesses());
        }
    }

    private void confirmAndReturnAccess(String subject, AccountAccess accountAccessFT, List<AccountAccess> accountAccesses) throws InsufficientPermissionException {
        accountAccesses.stream()
                .filter(a -> matchAccess(accountAccessFT, a))
                .findFirst().orElseThrow(() -> {
            String message = String.format(PERMISSION_MODEL_CHANGED_NO_SUFFICIENT_PERMISSION, subject, accountAccessFT.getIban());
            log.warn(message);
            return new InsufficientPermissionException(message);
        });
    }

    private boolean matchAccess(AccountAccess requested, AccountAccess existent) {
        return
                // Same iban
                StringUtils.equals(requested.getIban(), existent.getIban())
                        &&
                        // Make sure old access still valid
                        requested.getAccessType().compareTo(existent.getAccessType()) <= 0;
    }

    private BearerTokenBO getToken( AccessTokenBO loginToken, String authorisationId, TokenUsageBO usageType) throws UserNotFoundException {
        UserEntity user = userRepository.findById(loginToken.getSub()).orElseThrow(() -> new UserNotFoundException(CAN_NOT_LOAD_USER_WITH_ID + loginToken.getSub()));
        Date issueTime = new Date();
        Date expires = DateUtils.addSeconds(issueTime, defaultLoginTokenExpireInSeconds);

        return bearerTokenService.bearerToken(user.getId(), user.getLogin(),
                                              null, null, UserRole.valueOf(loginToken.getRole().name()),
                                              loginToken.getScaId(), authorisationId,
                                              issueTime, expires, usageType, null);
    }
}
