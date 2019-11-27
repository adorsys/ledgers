package de.adorsys.ledgers.um.impl.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.um.db.domain.UserRole;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.util.exception.UserManagementErrorCode.INSUFFICIENT_PERMISSION;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {
    private static final String NO_TRANSACTION_ACCESS_USER_DOES_NOT_HAVE_ACCESS = "No transaction access. User with id %s does not have access to accounts %s";
    private static final String NO_BALANCE_ACCESS_DOES_NOT_HAVE_ACCESS = "No balance access. User with id %s does not have access to accounts %s";
    private static final String NO_ACCOUNT_ACCESS_DOES_NOT_HAVE_ACCESS = "No account access. User with id %s does not have access to accounts %s";
    private static final String PERMISSION_MODEL_CHANGED_NO_SUFFICIENT_PERMISSION = "Permission model changed for user with subject %s no sufficient permission on account %s.";
    private static final String COULD_NOT_VERIFY_SIGNATURE_OF_TOKEN_WITH_SUBJECT = "Could not verify signature of token with subject : {}";
    private static final String TOKEN_WITH_SUBJECT_EXPIRED = "Token with subject {} is expired at {} and reference time is {}";
    private static final String WRONG_JWS_ALGO_FOR_TOKEN_WITH_SUBJECT = "Wrong jws algo for token with subject : {}";
    private static final String USER_DOES_NOT_HAVE_THE_ROLE_S = "User with id %s and login %s does not have the role %s";

    private static final int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.

    private final HashMacSecretSource secretSource;
    private final BearerTokenService bearerTokenService;
    private final UserService userService;
    private final PasswordEnc passwordEnc;

    @Override
    public BearerTokenBO authorise(String login, String pin, UserRoleBO role, String scaId, String authorisationId) {
        UserBO user = userService.findByLogin(login);
        boolean success = passwordEnc.verify(user.getId(), pin, user.getPin());
        if (!success) {
            return null;
        }

        // Check user has defined role.
        UserRoleBO userRole = user.getUserRoles().stream().filter(r -> r.name().equals(role.name()))
                                      .findFirst().orElseThrow(() -> UserManagementModuleException.builder()
                                                                             .errorCode(INSUFFICIENT_PERMISSION)
                                                                             .devMsg(String.format(USER_DOES_NOT_HAVE_THE_ROLE_S, user.getId(), user.getLogin(), role))
                                                                             .build());

        String scaIdParam = scaId != null
                                    ? scaId
                                    : Ids.id();
        String authorisationIdParam = authorisationId != null
                                              ? authorisationId
                                              : scaIdParam;

        Date issueTime = new Date();
        Date expires = DateUtils.addSeconds(issueTime, defaultLoginTokenExpireInSeconds);
        return bearerTokenService.bearerToken(user.getId(), user.getLogin(),
                null, null, UserRole.valueOf(userRole.name()), scaIdParam, authorisationIdParam, issueTime, expires, TokenUsageBO.LOGIN, null);
    }

    @Override
    public BearerTokenBO authorizeNewAuthorizationId(ScaInfoBO scaInfoBO, String authorizationId) {
        UserBO user = userService.findByLogin(scaInfoBO.getUserLogin());
        // Check user has defined role.
        UserRoleBO userRole = user.getUserRoles().stream().filter(r -> r.name().equals(scaInfoBO.getUserRole().name()))
                                      .findFirst().orElseThrow(() -> UserManagementModuleException.builder()
                                                                             .errorCode(INSUFFICIENT_PERMISSION)
                                                                             .devMsg(String.format(USER_DOES_NOT_HAVE_THE_ROLE_S, user.getId(), user.getLogin(), scaInfoBO.getUserRole()))
                                                                             .build());

        String scaIdParam = scaInfoBO.getScaId() != null
                                    ? scaInfoBO.getScaId()
                                    : Ids.id();
        String authorisationIdParam = authorizationId != null
                                              ? authorizationId
                                              : scaIdParam;

        Date issueTime = new Date();
        Date expires = DateUtils.addSeconds(issueTime, defaultLoginTokenExpireInSeconds);
        return bearerTokenService.bearerToken(user.getId(), user.getLogin(),
                null, null, UserRole.valueOf(userRole.name()), scaIdParam, authorisationIdParam, issueTime, expires, TokenUsageBO.LOGIN, null);
    }

    @Override
    public BearerTokenBO validate(String accessToken, Date refTime) {
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
            UserBO user = userService.findById(jwtClaimsSet.getSubject());

            AccessTokenBO accessTokenJWT = bearerTokenService.toAccessTokenObject(jwtClaimsSet);

            validateAccountAccesses(user, accessTokenJWT);
            AisConsentBO aisConsent = accessTokenJWT.getConsent();
            validateAisConsent(aisConsent, user);

            return bearerTokenService.bearerToken(accessToken, expiresIn, accessTokenJWT);

        } catch (ParseException | JOSEException e) {
            // If we can not parse the token, we log the error and return false.
            log.warn(e.getMessage());
            return null;
        }
    }

    @Override
    public BearerTokenBO consentToken(ScaInfoBO scaInfoBO, AisConsentBO aisConsent) {
        UserBO user = userService.findById(scaInfoBO.getUserId());
        aisConsent.setUserId(user.getId());
        validateAisConsent(aisConsent, user);
        Date issueTime = new Date();
        Date expires = getExpirationDate(aisConsent, issueTime);
        // Produce the token
        Map<String, String> act = new HashMap<>();
        String tppId = aisConsent.getTppId();
        act.put("tppId", tppId);
        UserRole userRole = UserRole.valueOf(scaInfoBO.getUserRole().name());
        return bearerTokenService.bearerToken(user.getId(), user.getLogin(), null, aisConsent, userRole,
                scaInfoBO.getScaId(), scaInfoBO.getAuthorisationId(), issueTime, expires, TokenUsageBO.DELEGATED_ACCESS, act);
    }

    @Override
    public BearerTokenBO scaToken(ScaInfoBO scaInfoBO) {
        return getToken(scaInfoBO, TokenUsageBO.DIRECT_ACCESS);
    }

    @Override
    public BearerTokenBO loginToken(ScaInfoBO scaInfoBO) {
        return getToken(scaInfoBO, TokenUsageBO.LOGIN);
    }

    private void validateAccountAccesses(UserBO user, AccessTokenBO accessTokenJWT) {
        List<AccountAccessBO> accountAccessesFromToken = accessTokenJWT.getAccountAccesses();
        for (AccountAccessBO accountAccessFT : accountAccessesFromToken) {
            confirmAndReturnAccess(user.getId(), accountAccessFT, user.getAccountAccesses());
        }
    }

    private void confirmAndReturnAccess(String subject, AccountAccessBO accountAccessFT, List<AccountAccessBO> accountAccesses) {
        accountAccesses.stream()
                .filter(a -> matchAccess(accountAccessFT, a))
                .findFirst()
                .orElseThrow(() -> {
                    String message = String.format(PERMISSION_MODEL_CHANGED_NO_SUFFICIENT_PERMISSION, subject, accountAccessFT.getIban());
                    log.warn(message);
                    return UserManagementModuleException.builder()
                                   .errorCode(INSUFFICIENT_PERMISSION)
                                   .devMsg(message)
                                   .build();
                });
    }

    private boolean matchAccess(AccountAccessBO requested, AccountAccessBO existent) {
        return
                // Same iban
                StringUtils.equals(requested.getIban(), existent.getIban())
                        // Same currency
                        && requested.getCurrency().equals(existent.getCurrency())
                        // Make sure old access still valid
                        && requested.getAccessType().compareTo(existent.getAccessType()) <= 0;
    }

    private BearerTokenBO getToken(ScaInfoBO scaInfoBO, TokenUsageBO usageType) {
        UserBO user = userService.findById(scaInfoBO.getUserId());
        Date issueTime = new Date();
        Date expires = DateUtils.addSeconds(issueTime, defaultLoginTokenExpireInSeconds);

        return bearerTokenService.bearerToken(user.getId(), user.getLogin(),
                null, null, UserRole.valueOf(scaInfoBO.getUserRole().name()),
                scaInfoBO.getScaId(), scaInfoBO.getAuthorisationId(),
                issueTime, expires, usageType, null);
    }

    private Date getExpirationDate(AisConsentBO aisConsent, Date iat) {
        LocalDate expirationLocalDate = aisConsent.getValidUntil();
        return expirationLocalDate == null
                       ? DateUtils.addDays(iat, 90) // default to 90 days
                       : Date.from(expirationLocalDate.atTime(23, 59, 59, 99).atZone(ZoneId.systemDefault()).toInstant());
    }

    private void validateAisConsent(AisConsentBO aisConsent, UserBO user) {
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

    /**
     * Makes sure the user has access to all those accounts.
     *
     * @param accessibleAccounts accessible account
     * @param requestedAccounts  requested accounts
     * @param message            message
     */
    private void checkAccountAccess(List<String> accessibleAccounts, List<String> requestedAccounts, String message, String userId) {
        ArrayList<String> copy = new ArrayList<>();
        if (requestedAccounts != null) {
            copy.addAll(requestedAccounts);
        }

        copy.removeAll(accessibleAccounts);

        if (!copy.isEmpty()) {
            throw UserManagementModuleException.builder()
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .devMsg(String.format(message, userId, copy.toString()))
                          .build();
        }
    }
}
