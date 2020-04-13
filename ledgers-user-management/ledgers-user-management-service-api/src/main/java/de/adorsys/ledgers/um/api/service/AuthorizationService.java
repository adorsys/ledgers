package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.ScaInfoBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;

import java.util.Date;

public interface AuthorizationService {

    /**
     * Verify user credential and produces a corresponding login token.
     * <p>
     * The granted access token can no be used to access account information.
     *
     * @param login           User login
     * @param pin             User PIN
     * @param role            the role of this user
     * @param scaId           the scaId
     * @param authorisationId the authorization id
     * @return BearerTokenBO representation of authorization status true for success
     */
    BearerTokenBO authorise(String login, String pin, UserRoleBO role, String scaId, String authorisationId);

    BearerTokenBO authorizeNewAuthorizationId(ScaInfoBO scaInfoBO, String authorizationId);

    /**
     * Check if the provided token is valid at the given reference time and return the corresponding user.
     *
     * @param accessToken the access token to validate
     * @param refTime     the reference time
     * @return the bearer token
     */
    BearerTokenBO validate(String accessToken, Date refTime);

    /**
     * Provides a token used to gain read access to an account.
     *
     * @param scaInfoBO SCA information
     * @param aisConsent  the ais consent.
     * @return the bearer token
     */
    BearerTokenBO consentToken(ScaInfoBO scaInfoBO, AisConsentBO aisConsent);

    /**
     * Create a new token for the current user, after a successful auth code process..
     *
     * @param scaInfoBO      : SCA information
     * @return the bearer token
     */
    BearerTokenBO scaToken(ScaInfoBO scaInfoBO);

    /**
     * Create a new token for the current user, with a new authorization id
     *
     * @param scaInfoBO      : SCA information
     * @return the bearer token
     */
    BearerTokenBO loginToken(ScaInfoBO scaInfoBO);

    boolean validateCredentials(String login, String pin, UserRoleBO role);
}
