package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.StartScaOprTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRedirectScaService;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseConverter;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_FULL_ACCESS;
import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_PARTIAL_ACCESS;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.CONSENT;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.valueOf;

@Service
@RequiredArgsConstructor
public class MiddlewareRedirectScaServiceImpl implements MiddlewareRedirectScaService {
    private static final String NO_USER_MESSAGE = "No user message";

    @Value("${ledgers.token.lifetime.seconds.full:7776000}")
    private int fullTokenLifeTime;
    @Value("${ledgers.sca.authCode.validity.seconds:180}")
    private int authCodeLifetime;
    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    private final UserService userService;
    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final AccessService accessService;
    private final ScaResponseConverter scaResponseConverter;
    private final ScaResponseMessageResolver messageResolver;
    private final KeycloakTokenService tokenService;
    private final BearerTokenMapper bearerTokenMapper;

    @Override
    public GlobalScaResponseTO startScaOperation(StartScaOprTO scaOpr, ScaInfoTO scaInfo) {
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());
        if (!user.hasSCA()) {
            throw MiddlewareModuleException.builder()
                          .errorCode(MiddlewareErrorCode.SCA_UNAVAILABLE)
                          .devMsg("Sorry, but do not have any SCA Methods configured!")
                          .build();
        }
        AuthCodeDataBO codeData = new AuthCodeDataBO(user.getLogin(), null, scaOpr.getOprId(), NO_USER_MESSAGE, authCodeLifetime,
                                                     valueOf(scaOpr.getOpType().name()), scaOpr.getAuthorisationId(), 100);
        SCAOperationBO operation = scaOperationService.checkIfExistsOrNew(codeData);

        try {
            return scaResponseConverter.mapResponse(operation, user.getScaUserData(), messageResolver.updateMessage(null, operation), bearerTokenMapper.toBearerTokenBO(scaInfo.getBearerToken()), 100, null);
        } catch (MiddlewareModuleException e) {
            throw scaOperationService.updateFailedCount(operation.getId(), true);
        }
    }

    @Override
    public GlobalScaResponseTO getMethods(String authorizationId, ScaInfoTO scaInfo) {
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());
        SCAOperationBO operation = scaOperationService.loadAuthCode(authorizationId);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), messageResolver.updateMessage(null, operation), bearerTokenMapper.toBearerTokenBO(scaInfo.getBearerToken()), 0, null);
    }

    @Override
    public GlobalScaResponseTO selectMethod(ScaInfoTO scaInfo) {
        SCAOperationBO operation = scaOperationService.loadAuthCode(scaInfo.getAuthorisationId());
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());
//        String psuMessage = messageResolver.getTemplate(operation);
        // TODO: create human-readable message (or message service).
        String psuMessage = "";
        int scaWeight = resolveWeightForOperation(operation.getOpType(), operation.getOpId(), user);
        AuthCodeDataBO data = new AuthCodeDataBO(user.getLogin(), scaInfo.getScaMethodId(), operation.getOpId(), psuMessage, authCodeLifetime, operation.getOpType(), operation.getId(), scaWeight);
        operation = scaOperationService.generateAuthCode(data, user, ScaStatusBO.SCAMETHODSELECTED);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), messageResolver.updateMessage(psuMessage, operation), bearerTokenMapper.toBearerTokenBO(scaInfo.getBearerToken()), scaWeight, null);
    }

    private int resolveWeightForOperation(OpTypeBO opType, String oprId, UserBO user) {
        Set<String> ibans = opType == CONSENT
                                    ? userService.loadConsent(oprId).getAccess().getAllAccounts()
                                    : Collections.singleton(paymentService.getPaymentById(oprId).getDebtorAccount().getIban());
        return accessService.resolveScaWeightCommon(ibans, user.getAccountAccesses());
    }

    @Override
    public GlobalScaResponseTO confirmAuthorization(ScaInfoTO scaInfo) {
        SCAOperationBO operation = scaOperationService.loadAuthCode(scaInfo.getAuthorisationId());
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());

        int scaWeight = resolveWeightForOperation(operation.getOpType(), operation.getOpId(), user);
        String psuMessage = messageResolver.getTemplate(operation);
        ScaValidationBO scaValidation = scaOperationService.validateAuthCode(scaInfo.getAuthorisationId(), operation.getOpId(), scaInfo.getAuthCode(), scaWeight);
        operation.setScaStatus(scaValidation.getScaStatus());
        boolean authenticationCompleted = scaOperationService.authenticationCompleted(operation.getOpId(), operation.getOpType());
        String scope = multilevelScaEnable && !authenticationCompleted
                               ? SCOPE_PARTIAL_ACCESS
                               : SCOPE_FULL_ACCESS;
        BearerTokenTO exchangeToken = tokenService.exchangeToken(scaInfo.getAccessToken(), fullTokenLifeTime, scope);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), messageResolver.updateMessage(psuMessage, operation), bearerTokenMapper.toBearerTokenBO(exchangeToken), scaWeight, scaValidation.getAuthConfirmationCode());
    }
}
