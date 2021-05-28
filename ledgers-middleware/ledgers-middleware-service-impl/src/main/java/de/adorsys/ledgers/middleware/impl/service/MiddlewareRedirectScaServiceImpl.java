package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.StartScaOprTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRedirectScaService;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseConverter;
import de.adorsys.ledgers.middleware.impl.service.message.PsuMessageResolver;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.CONSENT;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.valueOf;

@Service
@RequiredArgsConstructor
public class MiddlewareRedirectScaServiceImpl implements MiddlewareRedirectScaService {
    private static final String NO_USER_MESSAGE = "No user message";

    @Value("${ledgers.sca.authCode.validity.seconds:180}")
    private int authCodeLifetime;

    private final UserService userService;
    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final ScaResponseConverter scaResponseConverter;
    private final AccessService accessService;
    private final BearerTokenMapper bearerTokenMapper;
    private final PsuMessageResolver messageResolver;

    @Override
    public GlobalScaResponseTO startScaOperation(StartScaOprTO scaOpr, ScaInfoTO scaInfo) {
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());
        if (!user.hasSCA()) {
            throw MiddlewareModuleException.builder()
                          .errorCode(MiddlewareErrorCode.SCA_UNAVAILABLE)
                          .devMsg("Sorry, but do not have any SCA Methods configured!")
                          .build();
        }

        OpTypeBO opType = valueOf(scaOpr.getOpType().name());

        int scaWeight = resolveWeightForOperation(opType, scaOpr.getOprId(), user);
        AuthCodeDataBO codeData = new AuthCodeDataBO(user.getLogin(), null, scaOpr.getOprId(), scaOpr.getExternalId(), NO_USER_MESSAGE, authCodeLifetime,
                                                     opType, scaOpr.getAuthorisationId(), scaWeight);
        SCAOperationBO operation = scaOperationService.checkIfExistsOrNew(codeData);

        String psuMessage = messageResolver.message(StepOperation.START_SCA, operation);
        BearerTokenBO bearerToken = bearerTokenMapper.toBearerTokenBO(scaInfo.getBearerToken());
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), psuMessage, bearerToken, scaWeight, null);
    }

    @Override
    public GlobalScaResponseTO getMethods(String authorizationId, ScaInfoTO scaInfo) {
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());
        SCAOperationBO operation = scaOperationService.loadAuthCode(authorizationId);
        String psuMessage = messageResolver.message(StepOperation.GET_SCA_METHODS, operation);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), psuMessage, bearerTokenMapper.toBearerTokenBO(scaInfo.getBearerToken()), 0, null);
    }

    @Override
    public GlobalScaResponseTO selectMethod(ScaInfoTO scaInfo) {
        SCAOperationBO operation = scaOperationService.loadAuthCode(scaInfo.getAuthorisationId());
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());
        int scaWeight = resolveWeightForOperation(operation.getOpType(), operation.getOpId(), user);
        AuthCodeDataBO data = new AuthCodeDataBO(user.getLogin(), scaInfo.getScaMethodId(), operation.getOpId(),
                                                 null, "", authCodeLifetime, operation.getOpType(), operation.getId(), scaWeight);
        operation = scaOperationService.generateAuthCode(data, user, ScaStatusBO.SCAMETHODSELECTED);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), messageResolver.message(StepOperation.SELECT_SCA_METHOD, operation),
                                                bearerTokenMapper.toBearerTokenBO(scaInfo.getBearerToken()), scaWeight, null);
    }

    @Override
    public GlobalScaResponseTO confirmAuthorization(ScaInfoTO scaInfo) {
        SCAOperationBO operation = scaOperationService.loadAuthCode(scaInfo.getAuthorisationId());
        UserBO user = userService.findByLogin(scaInfo.getUserLogin());

        int scaWeight = resolveWeightForOperation(operation.getOpType(), operation.getOpId(), user);
        ScaValidationBO scaValidation = scaOperationService.validateAuthCode(scaInfo.getAuthorisationId(), operation.getOpId(), scaInfo.getAuthCode(), scaWeight);
        operation.setScaStatus(scaValidation.getScaStatus());
        boolean authenticationCompleted = scaOperationService.authenticationCompleted(operation.getOpId(), operation.getOpType());

        String psuMessage = messageResolver.message(StepOperation.CONFIRM_AUTH_CODE, operation);
        BearerTokenTO exchangeToken = accessService.exchangeTokenEndSca(authenticationCompleted, scaInfo.getAccessToken());
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), psuMessage, bearerTokenMapper.toBearerTokenBO(exchangeToken), scaWeight, scaValidation.getAuthConfirmationCode());
    }

    @Override
    public StartScaOprTO loadScaInformation(String authorizationId) {
        SCAOperationBO operation = scaOperationService.loadAuthCode(authorizationId);
        return new StartScaOprTO(operation.getOpId(), OpTypeTO.valueOf(operation.getOpType().name()));
    }

    private int resolveWeightForOperation(OpTypeBO opType, String oprId, UserBO user) {
        return opType == CONSENT
                       ? user.resolveMinimalWeightForIbanSet(userService.loadConsent(oprId).getUniqueIbans())
                       : user.resolveWeightForAccount(paymentService.getPaymentById(oprId).getAccountId());
    }
}
