package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaInfoMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

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
    private int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.

    @Override
    public SCALoginResponseTO authorise(String login, String pin, UserRoleTO role) {
        UserBO user = user(login);
        LoginKeyDataTO keyData = new LoginKeyDataTO(user.getId(), LocalDateTime.now());
        String opId = keyData.toOpId();
        String authorisationId = opId;
        String scaId = opId;
        BearerTokenBO loginTokenBO = proceedToLogin(user, pin, role, scaId, authorisationId);
        if (!scaRequired(user, OpTypeBO.LOGIN)) {
            return authorizeResponse(loginTokenBO);
        } else {
            SCAOperationBO scaOperationBO;
            UserTO userTo = scaUtils.user(user);
            String scaUserDataId = null;
            String opData = opId;
            AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), scaUserDataId,
                    keyData.toOpId(), opData, keyData.messageTemplate(),
                    defaultLoginTokenExpireInSeconds, OpTypeBO.LOGIN, authorisationId, 0);
            if (userTo.getScaUserData().size() == 1) {
                ScaUserDataTO chosenScaMethod = userTo.getScaUserData().iterator().next();
                authCodeData.setScaUserDataId(chosenScaMethod.getId());
                scaOperationBO = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
            } else {
                scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUIDENTIFIED);
            }
            SCALoginResponseTO response = toScaResponse(user, keyData.messageTemplate(), scaOperationBO);
            userService.loginToken(loginTokenBO.getAccessTokenObject().buildScaInfoBO());
            response.setBearerToken(bearerTokenMapper.toBearerTokenTO(loginTokenBO));
            return response;
        }
    }

    @Override
    public SCALoginResponseTO authoriseForConsent(String login, String pin, String consentId, String authorisationId, OpTypeTO opType) {
        OpTypeBO opTypeBO = OpTypeBO.valueOf(opType.name());
        UserBO user = user(login);
        // FOr login we use the login name and login time for authId and authorizationId.
        BearerTokenBO loginTokenBO = proceedToLogin(user, pin, UserRoleTO.CUSTOMER, consentId, authorisationId);
        if (!scaRequired(user, opTypeBO)) {
            return authorizeResponse(loginTokenBO);
        } else {
            AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), null,
                    consentId, null, NO_USER_MESSAGE,
                    defaultLoginTokenExpireInSeconds, opTypeBO, authorisationId, 0);
            SCAOperationBO scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUIDENTIFIED);
            SCALoginResponseTO response = toScaResponse(user, NO_USER_MESSAGE, scaOperationBO);
            BearerTokenBO scaTokenBO = userService.scaToken(loginTokenBO.getAccessTokenObject().buildScaInfoBO());
            response.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaTokenBO));
            return response;
        }
    }

    @Override
    public BearerTokenTO validate(String accessToken) {
        return bearerTokenMapper.toBearerTokenTO(userService.validate(accessToken, new Date()));
    }

    @Override
    public UserTO register(String login, String email, String pin, UserRoleTO role) {
        UserTO user = new UserTO(login, email, pin);
        log.info(user.toString());
        user.getUserRoles().add(role);
        UserBO userBO = userTOMapper.toUserBO(user);
        return userTOMapper.toUserTO(userService.create(userBO));
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public SCALoginResponseTO generateLoginAuthCode(ScaInfoTO scaInfoTO, String userMessage, int validitySeconds) {
        UserBO user = scaUtils.userBO(scaInfoTO.getUserId());
        SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(scaInfoTO.getAuthorisationId());
        LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
        String opId = scaOperationBO.getOpId();
        String opData = opId;
        AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), scaInfoTO.getScaMethodId(),
                opId, opData, userMessage, validitySeconds,
                OpTypeBO.LOGIN, scaInfoTO.getAuthorisationId(), 0);
        scaOperationBO = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
        SCALoginResponseTO scaResponse = toScaResponse(user, keyData.messageTemplate(), scaOperationBO);
        BearerTokenBO loginToken = userService.loginToken(scaInfoMapper.toScaInfoBO(scaInfoTO));
        scaResponse.setBearerToken(bearerTokenMapper.toBearerTokenTO(loginToken));
        return scaResponse;
    }

    @Override
    public SCALoginResponseTO authenticateForLogin(ScaInfoTO scaInfoTO) {
        UserBO user = scaUtils.userBO(scaInfoTO.getUserId());
        SCAOperationBO scaOperationBO = scaOperationService.loadAuthCode(scaInfoTO.getAuthorisationId());
        LoginKeyDataTO keyData = LoginKeyDataTO.fromOpId(scaOperationBO.getOpId());
        String authorisationId = scaInfoTO.getAuthorisationId();
        boolean valid = scaOperationService.validateAuthCode(authorisationId, authorisationId, authorisationId, scaInfoTO.getAuthCode(), 0);
        SCALoginResponseTO scaResponse = toScaResponse(user, keyData.messageTemplate(), scaOperationBO);
        if (valid) {
            BearerTokenBO scaToken = userService.scaToken(scaInfoMapper.toScaInfoBO(scaInfoTO));
            scaResponse.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaToken));
        }
        return scaResponse;
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

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private boolean scaRequired(UserBO user, OpTypeBO opType) {
        return scaUtils.hasSCA(user);
    }

    private SCALoginResponseTO authorizeResponse(BearerTokenBO loginTokenBO) {
        SCALoginResponseTO response = new SCALoginResponseTO();
        response.setScaStatus(ScaStatusTO.EXEMPTED);
        BearerTokenBO scaTokenBO = userService.scaToken(loginTokenBO.getAccessTokenObject().buildScaInfoBO());
        response.setBearerToken(bearerTokenMapper.toBearerTokenTO(scaTokenBO));
        response.setScaId(scaTokenBO.getAccessTokenObject().getScaId());
        response.setExpiresInSeconds(scaTokenBO.getExpires_in());
        response.setStatusDate(LocalDateTime.now());
        return response;
    }

    private UserBO user(String login) {
        return userService.findByLogin(login);
    }

    private BearerTokenBO proceedToLogin(UserBO user, String pin, UserRoleTO role, String scaId, String authorisationId) {
        UserRoleBO roleBo = UserRoleBO.valueOf(role.name());
        // FOr login we use the login name and login time for authId and authorizationId.
        BearerTokenBO loginTokenBO = userService.authorise(user.getLogin(), pin, roleBo, scaId, authorisationId);
        if (loginTokenBO == null) {
            throw new InsufficientPermissionMiddlewareException("Unknown credentials.");
        }
        return loginTokenBO;
    }
}
