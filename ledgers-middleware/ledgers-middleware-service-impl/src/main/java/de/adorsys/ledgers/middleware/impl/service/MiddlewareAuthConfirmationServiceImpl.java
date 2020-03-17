package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthConfirmationTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAuthConfirmationService;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.ScaAuthConfirmationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.PATC;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.RJCT;

@Service
@RequiredArgsConstructor
public class MiddlewareAuthConfirmationServiceImpl implements MiddlewareAuthConfirmationService {
    private final SCAOperationService scaOperationService;
    private final DepositAccountPaymentService depositAccountPaymentService;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Override
    public AuthConfirmationTO verifyAuthConfirmationCode(String authorisationId, String authConfirmCode, String userLogin) {
        ScaAuthConfirmationBO authConfirmationBO = scaOperationService.verifyAuthConfirmationCode(authorisationId, authConfirmCode);
        if (authConfirmationBO.isConfirm()) {
            return buildAuthConfirmationTO(userLogin, authConfirmationBO);
        }
        return new AuthConfirmationTO()
                       .success(authConfirmationBO.isConfirm());
    }

    @Override
    public AuthConfirmationTO completeAuthConfirmation(String authorisationId, boolean authCodeConfirmed, String userLogin) {
        ScaAuthConfirmationBO authConfirmationBO = scaOperationService.completeAuthConfirmation(authorisationId, authCodeConfirmed);
        if (authConfirmationBO.isConfirm()) {
            return buildAuthConfirmationTO(userLogin, authConfirmationBO);
        }
        AuthConfirmationTO confirmation = new AuthConfirmationTO().success(false);
        if (EnumSet.of(OpTypeBO.PAYMENT, OpTypeBO.CANCEL_PAYMENT).contains(authConfirmationBO.getOpTypeBO())) {
            TransactionStatusBO status = depositAccountPaymentService.updatePaymentStatus(authConfirmationBO.getOpId(), RJCT);
            confirmation.transactionStatus(TransactionStatusTO.valueOf(status.toString()));
        }
        return confirmation;
    }

    private AuthConfirmationTO buildAuthConfirmationTO(String userLogin, ScaAuthConfirmationBO authConfirmationBO) {
        AuthConfirmationTO confirmation = new AuthConfirmationTO().success(true);
        boolean authCompleted = scaOperationService.authenticationCompleted(authConfirmationBO.getOpId(), authConfirmationBO.getOpTypeBO());
        if (EnumSet.of(OpTypeBO.PAYMENT, OpTypeBO.CANCEL_PAYMENT).contains(authConfirmationBO.getOpTypeBO())) {
            if (authCompleted) {
                depositAccountPaymentService.updatePaymentStatus(authConfirmationBO.getOpId(), authConfirmationBO.getOpTypeBO() == OpTypeBO.PAYMENT
                                                                                                       ? TransactionStatusBO.ACTC
                                                                                                       : TransactionStatusBO.CANC);
                TransactionStatusBO status = depositAccountPaymentService.executePayment(authConfirmationBO.getOpId(), userLogin);
                confirmation.transactionStatus(TransactionStatusTO.valueOf(status.toString()));
            } else if (multilevelScaEnable) {
                TransactionStatusBO status = depositAccountPaymentService.updatePaymentStatus(authConfirmationBO.getOpId(), PATC);
                confirmation.transactionStatus(TransactionStatusTO.valueOf(status.toString()));
            }
        } else if (OpTypeBO.CONSENT == authConfirmationBO.getOpTypeBO() && !authCompleted) {
            confirmation.partiallyAuthorised(multilevelScaEnable);
            confirmation.multilevelScaRequired(multilevelScaEnable);
        }
        return confirmation;
    }
}
