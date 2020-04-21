package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.LoginKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaInfoMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewareOnlineBankingServiceImpl implements MiddlewareOnlineBankingService {
    private static final String NO_USER_MESSAGE = "No user message";
    private final UserMapper userTOMapper = Mappers.getMapper(UserMapper.class);

    private final UserService userService;
    private final BearerTokenMapper bearerTokenMapper;
    private final SCAOperationService scaOperationService;
    private final SCAUtils scaUtils;
    private final ScaInfoMapper scaInfoMapper;
    private final AuthorizationService authorizationService;

    @Value("${default.token.lifetime.seconds:600}")
    private int defaultLoginTokenExpireInSeconds;

    @Override
    public SCALoginResponseTO authorise(String login, String pin, UserRoleTO role) {
        UserBO user = user(login);
        LoginKeyDataTO keyData = new LoginKeyDataTO(user.getId(), LocalDateTime.now());
        String opId = keyData.toOpId();
        BearerTokenBO loginTokenBO = proceedToLogin(user, pin, role, opId, opId);
        return authorizeResponse(loginTokenBO);
    }

    @Override
    @Transactional(noRollbackFor = MiddlewareModuleException.class)
    public SCALoginResponseTO authoriseForConsent(String login, String pin, String consentId, String authorisationId, OpTypeTO opType) {
        OpTypeBO opTypeBO = OpTypeBO.valueOf(opType.name());
        UserBO user = user(login);
        try {
            scaOperationService.checkIfExistsOrNew(new AuthCodeDataBO(user.getLogin(), null, consentId, null, NO_USER_MESSAGE,
                                                                      defaultLoginTokenExpireInSeconds, opTypeBO, authorisationId, 0));
            BearerTokenBO loginTokenBO = proceedToLogin(user, pin, UserRoleTO.CUSTOMER, consentId, authorisationId);
            return resolveLoginResponseForConsentLogin(consentId, authorisationId, opTypeBO, user, loginTokenBO);
        } catch (MiddlewareModuleException | ScaModuleException e) {
            int attemptsLeft = scaOperationService.updateFailedCount(authorisationId);
            String errorMsgPart = attemptsLeft > 0
                                          ? String.format("You have %s attempts to enter valid credentials", attemptsLeft)
                                          : "Your Login authorization is FAILED please create a new one.";
            MiddlewareErrorCode code = attemptsLeft > 0
                                               ? PSU_AUTH_ATTEMPT_INVALID
                                               : AUTHENTICATION_FAILURE;
            throw MiddlewareModuleException.builder()
                          .devMsg("Either your login or pin is incorrect. " + errorMsgPart)
                          .errorCode(code)
                          .build();
        }
    }

    @Override
    public SCALoginResponseTO authoriseForConsentWithToken(ScaInfoTO scaInfo, String consentId, String authorisationId, OpTypeTO opType) {
        OpTypeBO opTypeBO = OpTypeBO.valueOf(opType.name());
        UserBO user = user(scaInfo.getUserLogin());

        BearerTokenBO loginTokenBO = proceedToLogin(scaInfoMapper.toScaInfoBO(scaInfo), authorisationId);
        return resolveLoginResponseForConsentLogin(consentId, authorisationId, opTypeBO, user, loginTokenBO);
    }

    @NotNull
    private SCALoginResponseTO resolveLoginResponseForConsentLogin(String consentId, String authorisationId, OpTypeBO opTypeBO, UserBO user, BearerTokenBO loginTokenBO) {
        if (!scaRequired(user)) {
            return authorizeResponse(loginTokenBO);
        } else {
            AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), null, consentId, null, NO_USER_MESSAGE,
                                                             defaultLoginTokenExpireInSeconds, opTypeBO, authorisationId, 0);
            SCAOperationBO scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUIDENTIFIED);
            SCALoginResponseTO response = toScaResponse(user, NO_USER_MESSAGE, scaOperationBO);
            BearerTokenBO scaTokenBO = authorizationService.scaToken(loginTokenBO.getAccessTokenObject().buildScaInfoBO());
            response.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaTokenBO));
            return response;
        }
    }

    private BearerTokenBO proceedToLogin(ScaInfoBO scaInfo, String authorisationId) {
        return Optional.ofNullable(authorizationService.authorizeNewAuthorizationId(scaInfo, authorisationId))
                       .orElseThrow(() -> MiddlewareModuleException.builder()
                                                  .errorCode(INSUFFICIENT_PERMISSION)
                                                  .devMsg("Unknown credentials.")
                                                  .build());
    }

    @Override
    public BearerTokenTO validate(String accessToken) {
        return bearerTokenMapper.toBearerTokenTO(authorizationService.validate(accessToken, new Date()));
    }

    @Override
    public UserTO register(String login, String email, String pin, UserRoleTO role) {
        UserTO user = new UserTO(login, email, pin);
        user.getUserRoles().add(role);
        UserBO userBO = userTOMapper.toUserBO(user);
        return userTOMapper.toUserTO(userService.create(userBO));
    }

    @Override
    public SCALoginResponseTO generateLoginAuthCode(ScaInfoTO scaInfoTO, String userMessage, int validitySeconds) {
        UserBO user = scaUtils.userBO(scaInfoTO.getUserId());
        SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(scaInfoTO.getAuthorisationId());
        LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
        String opId = scaOperationBO.getOpId();
        AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), scaInfoTO.getScaMethodId(),
                                                         opId, opId, userMessage, validitySeconds,
                                                         OpTypeBO.LOGIN, scaInfoTO.getAuthorisationId(), 0);
        scaOperationBO = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
        SCALoginResponseTO scaResponse = toScaResponse(user, keyData.messageTemplate(), scaOperationBO);
        BearerTokenBO loginToken = authorizationService.loginToken(scaInfoMapper.toScaInfoBO(scaInfoTO));
        scaResponse.setBearerToken(bearerTokenMapper.toBearerTokenTO(loginToken));
        return scaResponse;
    }

    @Override
    public SCALoginResponseTO authenticateForLogin(ScaInfoTO scaInfoTO) {
        UserBO user = scaUtils.userBO(scaInfoTO.getUserId());
        SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(scaInfoTO.getAuthorisationId());
        LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
        String authorisationId = scaInfoTO.getAuthorisationId();
        ScaValidationBO scaValidationBO = scaOperationService.validateAuthCode(authorisationId, authorisationId, authorisationId, scaInfoTO.getAuthCode(), 0);
        SCALoginResponseTO scaResponse = toScaResponse(user, keyData.messageTemplate(), scaOperationBO);
        if (scaValidationBO.isValidAuthCode()) {
            BearerTokenBO scaToken = authorizationService.scaToken(scaInfoMapper.toScaInfoBO(scaInfoTO));
            scaResponse.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaToken));
            scaResponse.setAuthConfirmationCode(scaValidationBO.getAuthConfirmationCode());
        }
        return scaResponse;
    }

    @Override
    public SCALoginResponseTO authorizeForUser(String login, String pin, String userLogin) {
        boolean isValid = authorizationService.validateCredentials(login, pin, UserRoleBO.SYSTEM);
        if (!isValid) {
            throw MiddlewareModuleException.builder()
                          .devMsg("Your credentials or role does not comply to request you're executing!")
                          .errorCode(AUTHENTICATION_FAILURE)
                          .build();
        }

        SCALoginResponseTO response = new SCALoginResponseTO();
        response.setScaStatus(ScaStatusTO.EXEMPTED);
        UserBO user = user(userLogin);
        BearerTokenBO scaTokenBO = authorizationService.scaToken(new ScaInfoBO(user.getId(), null, null, UserRoleBO.CUSTOMER, null, null, TokenUsageBO.DIRECT_ACCESS, user.getLogin()));
        response.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaTokenBO));
        response.setScaId(scaTokenBO.getAccessTokenObject().getScaId());
        response.setExpiresInSeconds(scaTokenBO.getExpires_in());
        response.setStatusDate(LocalDateTime.now());
        return response;
    }

    private SCALoginResponseTO toScaResponse(UserBO user, String userMessage,
                                             SCAOperationBO a) {
        SCALoginResponseTO response = new SCALoginResponseTO();
        UserTO userTO = scaUtils.user(user);
        response.setAuthorisationId(a.getId());
        response.setChosenScaMethod(scaUtils.getScaMethod(userTO, a.getScaMethodId()));
        response.setChallengeData(null);
        response.setExpiresInSeconds(a.getValiditySeconds());
        response.setScaId(a.getOpId());
        response.setPsuMessage(userMessage);
        response.setScaMethods(userTO.getScaUserData());
        response.setScaStatus(ScaStatusTO.valueOf(a.getScaStatus().name()));
        response.setStatusDate(a.getStatusTime());
        return response;
    }

    private boolean scaRequired(UserBO user) {
        return scaUtils.hasSCA(user);
    }

    private SCALoginResponseTO authorizeResponse(BearerTokenBO loginTokenBO) {
        SCALoginResponseTO response = new SCALoginResponseTO();
        response.setScaStatus(ScaStatusTO.EXEMPTED);
        BearerTokenBO scaTokenBO = authorizationService.scaToken(loginTokenBO.getAccessTokenObject().buildScaInfoBO());
        response.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaTokenBO));
        response.setScaId(scaTokenBO.getAccessTokenObject().getScaId());
        response.setExpiresInSeconds(scaTokenBO.getExpires_in());
        response.setStatusDate(LocalDateTime.now());
        response.setAuthorisationId(loginTokenBO.getAccessTokenObject().getAuthorisationId());
        return response;
    }

    private UserBO user(String login) {
        return userService.findByLogin(login);
    }

    private BearerTokenBO proceedToLogin(UserBO user, String pin, UserRoleTO role, String scaId, String authorisationId) {
        UserRoleBO roleBo = UserRoleBO.valueOf(role.name());
        // FOr login we use the login name and login time for authId and authorizationId.
        return Optional.ofNullable(authorizationService.authorise(user.getLogin(), pin, roleBo, scaId, authorisationId))
                       .orElseThrow(() -> MiddlewareModuleException.builder()
                                                  .errorCode(INSUFFICIENT_PERMISSION)
                                                  .devMsg("Unknown credentials.")
                                                  .build());
    }
}
