package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.ConsentKeyDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.impl.converter.AisConsentBOMapper;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.CONSENT;
import static de.adorsys.ledgers.sca.domain.ScaStatusBO.FINALISED;
import static de.adorsys.ledgers.sca.domain.ScaStatusBO.SCAMETHODSELECTED;

@Service
@RequiredArgsConstructor
public class ScaResponseMessageResolver {
    private final DepositAccountPaymentService paymentService;
    private final UserService userService;
    private final PaymentCoreDataPolicy coreDataPolicy;
    private final AisConsentBOMapper aisConsentMapper;

    public String getTemplate(SCAOperationBO scaOperation) {
        OpTypeBO opType = scaOperation.getOpType();
        String oprId = scaOperation.getOpId();

        return resolveOperationMessage(opType, oprId);
    }

    private String resolveOperationMessage(OpTypeBO opType, String oprId) {
        if (opType == CONSENT) {
            AisConsentTO aisConsent = aisConsentMapper.toAisConsentTO(userService.loadConsent(oprId));
            return new ConsentKeyDataTO(aisConsent).template();
        }
        PaymentBO payment = paymentService.getPaymentById(oprId);
        return coreDataPolicy.getPaymentCoreData(opType, payment).getTanTemplate();
    }

    public String updateMessage(String template, SCAOperationBO operation) {
        if (SCAMETHODSELECTED.equals(operation.getScaStatus())) {
            return String.format(template, operation.getTan());
        } else if (FINALISED.equals(operation.getScaStatus())) {
            return String.format("Your %s id: %s is confirmed", operation.getOpType(), operation.getOpId());
        } else {
            return String.format("Your Login for %s id: %s is successful", operation.getOpType(), operation.getOpId());
        }
    }
}
