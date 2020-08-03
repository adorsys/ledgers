package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaDataInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeDataResolver;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ScaResponseConverter {
    private final UserMapper userMapper;
    private final ScaChallengeDataResolver scaChallengeDataResolver;
    private final SCAOperationService scaOperationService;
    private final BearerTokenMapper tokenMapper;

    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    public GlobalScaResponseTO mapResponse(SCAOperationBO operation, List<ScaUserDataBO> methods, TransactionStatusBO transactionStatus,
                                           String psuMessage, BearerTokenBO token, int scaWeight, String authConfirmationCode) {
        GlobalScaResponseTO response = new GlobalScaResponseTO();
        response.setOperationObjectId(operation.getOpId());
        response.setAuthorisationId(operation.getId());
        response.setScaMethods(userMapper.toScaUserDataListTO(methods));
        response.setPsuMessage(psuMessage);
        response.setStatusDate(operation.getStatusTime());
        response.setBearerToken(tokenMapper.toBearerTokenTO(token));
        response.setExpiresInSeconds(operation.getValiditySeconds());
        response.setTan(operation.getTan());
        mapEnum(operation.getOpType(), OpTypeTO.class, response::setOpType);
        mapEnum(transactionStatus, TransactionStatusTO.class, response::setTransactionStatus);
        mapEnum(operation.getScaStatus(), ScaStatusTO.class, response::setScaStatus);

        if (response.getScaStatus() == ScaStatusTO.SCAMETHODSELECTED) {
            methods.stream()
                    .filter(m -> m.getId().equals(operation.getScaMethodId()))
                    .findFirst()
                    .map(userMapper::toScaUserDataTO)
                    .ifPresent(m -> response.setChallengeData(scaChallengeDataResolver.resolveScaChallengeData(m.getScaMethod())
                                                                      .getChallengeData(new ScaDataInfoTO(m, operation.getTan()))));

        }

        if (multilevelScaEnable) {
            response.setMultilevelScaRequired(scaWeight < 100);
            boolean completed = scaOperationService.authenticationCompleted(operation.getOpId(), operation.getOpType());
            response.setPartiallyAuthorised(!completed);
        }
        Optional.ofNullable(authConfirmationCode).ifPresent(response::setAuthConfirmationCode);
        return response;
    }

    private <T extends Enum, R extends Enum> void mapEnum(R mapped, Class<T> mapTo, Consumer<T> consumer) {
        Optional.ofNullable(mapped)
                .map(Enum::name)
                .map(n -> Enum.valueOf(mapTo, n))
                .ifPresent(t -> consumer.accept((T) t));
    }
}
