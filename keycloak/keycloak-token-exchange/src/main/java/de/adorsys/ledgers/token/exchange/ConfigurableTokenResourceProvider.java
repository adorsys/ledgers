/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.token.exchange;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.Cors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.keycloak.services.resources.Cors.ACCESS_CONTROL_ALLOW_METHODS;
import static org.keycloak.services.resources.Cors.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.keycloak.services.util.DefaultClientSessionContext.fromClientSessionScopeParameter;

/**
 * @author Lorent Lempereur
 */
@SuppressWarnings("PMD")
public class ConfigurableTokenResourceProvider implements RealmResourceProvider {

    static final String ID = "configurable-token";
    private static final Logger LOG = Logger.getLogger(ConfigurableTokenResourceProvider.class);

    private final KeycloakSession session;
    private final TokenManager tokenManager;

    ConfigurableTokenResourceProvider(KeycloakSession session) {
        this.session = session;
        this.tokenManager = new TokenManager();
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
        //This is a generated stub
    }

    @OPTIONS
    public Response preflight() {
        KeycloakContext context = session.getContext();
        return Cors.add(context.getHttpRequest(), Response.ok())
                       .auth()
                       .preflight()
                       .allowedMethods("POST", "OPTIONS")
                       .build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response createToken(TokenConfiguration tokenConfiguration) {
        try {
            KeycloakContext context = session.getContext();
            HttpRequest request = context.getHttpRequest();
            AccessToken accessToken = validateTokenAndUpdateSession(request);
            UserSessionModel userSession = this.findSession();
            AccessTokenResponse response = this.createAccessToken(userSession, accessToken, tokenConfiguration);

            return this.buildCorsResponse(request, response);
        } catch (ConfigurableTokenException e) {
            LOG.error("An error occurred when fetching an access token", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private AccessTokenResponse createAccessToken(UserSessionModel userSession,
                                                  AccessToken accessToken,
                                                  TokenConfiguration tokenConfiguration) {
        RealmModel realm = this.session.getContext().getRealm();
        ClientModel client = realm.getClientByClientId(accessToken.getIssuedFor());
        LOG.infof("Configurable token requested for username=%s and client=%s on realm=%s", userSession.getUser().getUsername(), client.getClientId(), realm.getName());
        AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());
        ClientSessionContext clientSessionContext = fromClientSessionScopeParameter(clientSession, session);

        AccessToken newToken = tokenManager.createClientAccessToken(session, realm, client, userSession.getUser(), userSession, clientSessionContext);
        updateTokenExpiration(newToken, tokenConfiguration);
        updateScope(newToken, tokenConfiguration);
        return buildResponse(realm, userSession, client, clientSession, newToken);
    }

    private AccessToken validateTokenAndUpdateSession(HttpRequest request) throws ConfigurableTokenException {
        try {
            RealmModel realm = session.getContext().getRealm();
            String tokenString = readAccessTokenFrom(request);
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(tokenString, AccessToken.class).withChecks(
                    TokenVerifier.IS_ACTIVE,
                    new TokenVerifier.RealmUrlCheck(Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()))
            );
            SignatureVerifierContext verifierContext = session.getProvider(SignatureProvider.class, verifier.getHeader().getAlgorithm().name()).verifier(verifier.getHeader().getKeyId());
            verifier.verifierContext(verifierContext);
            AccessToken accessToken = verifier.verify().getToken();
            if (!tokenManager.checkTokenValidForIntrospection(session, realm, accessToken, false)) {
                throw new VerificationException("introspection_failed");
            }
            return accessToken;
        } catch (ConfigurableTokenException e) {
            throw e;
        } catch (VerificationException e) {
            LOG.warn("Keycloak-ConfigurableToken: introspection of token failed", e);
            throw new ConfigurableTokenException("access_token_introspection_failed: " + e.getMessage());
        }
    }

    private String readAccessTokenFrom(HttpRequest request) throws ConfigurableTokenException {
        String authorization = request.getHttpHeaders().getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            LOG.warn("Keycloak-ConfigurableToken: no authorization header with bearer token");
            throw new ConfigurableTokenException("bearer_token_missing_in_authorization_header");
        }
        String token = authorization.substring(7);
        if (token.isEmpty()) {
            LOG.warn("Keycloak-ConfigurableToken: empty access token");
            throw new ConfigurableTokenException("missing_access_token");
        }
        return token;
    }

    private UserSessionModel findSession() throws ConfigurableTokenException {
        AuthenticationManager.AuthResult authenticated = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();

        if (authenticated == null) {
            LOG.warn("Keycloak-ConfigurableToken: user not authenticated");
            throw new ConfigurableTokenException("not_authenticated");
        }

        if (authenticated.getToken().getRealmAccess() == null) {
            LOG.warn("Keycloak-ConfigurableToken: no realm associated with authorization");
            throw new ConfigurableTokenException("wrong_realm");
        }

        UserModel user = authenticated.getUser();
        if (user == null || !user.isEnabled()) {
            LOG.warn("Keycloak-ConfigurableToken: user does not exist or is not enabled");
            throw new ConfigurableTokenException("invalid_user");
        }

        UserSessionModel userSession = authenticated.getSession();
        if (userSession == null) {
            LOG.warn("Keycloak-ConfigurableToken: user does not have any active session");
            throw new ConfigurableTokenException("missing_user_session");
        }

        return userSession;
    }


    private Response buildCorsResponse(@Context HttpRequest request, AccessTokenResponse response) {
        Cors cors = Cors.add(request)
                            .auth()
                            .allowedMethods("POST")
                            .auth()
                            .exposedHeaders(ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_ORIGIN)
                            .allowAllOrigins();
        return cors.builder(Response.ok(response).type(MediaType.APPLICATION_JSON_TYPE)).build();
    }


    private AccessTokenResponse buildResponse(RealmModel realm,
                                              UserSessionModel userSession,
                                              ClientModel client,
                                              AuthenticatedClientSessionModel clientSession,
                                              AccessToken token) {
        EventBuilder eventBuilder = new EventBuilder(realm, session, session.getContext().getConnection());
        ClientSessionContext clientSessionContext = fromClientSessionScopeParameter(clientSession, session);
        return tokenManager.responseBuilder(realm, client, eventBuilder, session, userSession, clientSessionContext)
                       .accessToken(token)
                       .build();
    }

    private void updateTokenExpiration(AccessToken token, TokenConfiguration tokenConfiguration) {
        token.expiration(tokenConfiguration.computeTokenExpiration(token.getExp().intValue(), true));
    }

    private void updateScope(AccessToken token, TokenConfiguration tokenConfiguration) {
        String offlineAccess = token.getScope().contains("offline_access") ? " offline_access" : "";
        String updatedScope = token.getScope() + " " + tokenConfiguration.getScope() + offlineAccess;
        token.setScope(updatedScope.trim());
    }

    static class ConfigurableTokenException extends Exception {
        public ConfigurableTokenException(String message) {
            super(message);
        }
    }
}
