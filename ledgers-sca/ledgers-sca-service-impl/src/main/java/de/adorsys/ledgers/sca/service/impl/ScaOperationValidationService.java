/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.domain.ScaStatus;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.*;
import static de.adorsys.ledgers.util.exception.SCAErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
class ScaOperationValidationService {
    private static final String STOLEN_ERROR = "Seems auth code was stolen because it already used in the system";
    private static final String EXPIRATION_ERROR = "Operation is not valid because of expiration";

    @Value("${ledgers.sca.final.weight:100}")
    private int finalWeight;
    @Value("${ledgers.sca.authCode.failed.max:5}")
    private int authCodeFailedMax;
    @Value("${ledgers.sca.login.failed.max:3}")
    private int loginFailedMax;
    private final SCAOperationRepository repository;

    public void checkValidityAndAttempts(SCAOperationEntity scaOperation, AuthCodeDataBO data, UserBO user) {
        checkOperationAttempts(scaOperation, false);
        checkScaOperationIsValid(data, user, scaOperation);
    }

    public void checkAll(SCAOperationEntity scaOperation, String opId) {
        checkOperationAttempts(scaOperation, false);
        checkOperationNotUsed(scaOperation);
        checkOperationNotExpired(scaOperation);
        checkSameOperation(scaOperation, opId);
    }

    public void checksOnPresentOperation(SCAOperationEntity scaOperation) {
        checkOperationAttempts(scaOperation, true);
        checkOperationNotUsed(scaOperation);
        checkOperationNotExpired(scaOperation);
    }

    public boolean isMultiLevelScaCompleted(List<SCAOperationEntity> found, OpTypeBO opType) {
        return EnumSet.of(PAYMENT, CANCEL_PAYMENT, CONSENT).contains(opType)
                       && isCompletedByAllUsers(found);
    }

    public boolean isAnyScaCompleted(List<SCAOperationEntity> found) {
        return found.stream()
                       .anyMatch(op -> op.getScaStatus() == ScaStatus.FINALISED);
    }

    private void checkOperationNotUsed(SCAOperationEntity operation) {
        if (operation.isOperationAlreadyUsed()) {
            log.error(STOLEN_ERROR);
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_USED_OR_STOLEN)
                          .devMsg(STOLEN_ERROR)
                          .build();
        }
    }

    private void checkOperationNotExpired(SCAOperationEntity operation) {
        if (operation.isOperationExpired()) {
            operation.expireOperation();
            repository.save(operation);
            log.error(EXPIRATION_ERROR);
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_EXPIRED)
                          .devMsg(EXPIRATION_ERROR)
                          .build();
        }
    }

    private void checkSameOperation(SCAOperationEntity operation, String opId) {
        if (!StringUtils.equals(opId, operation.getOpId())) {
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_VALIDATION_INVALID)
                          .devMsg("Operation id not matching.")
                          .build();
        }
    }

    private boolean isCompletedByAllUsers(List<SCAOperationEntity> found) {
        return found.stream()
                       .filter(op -> op.getScaStatus() == ScaStatus.FINALISED)
                       .collect(Collectors.summarizingInt(SCAOperationEntity::getScaWeight))
                       .getSum() >= finalWeight;
    }

    private void checkScaOperationIsValid(AuthCodeDataBO data, UserBO user, SCAOperationEntity scaOperation) {
        // One sca method is set, we do not change it anymore.
        if (scaOperation.getScaMethodId() == null) {
            if (data.getScaUserDataId() == null) {
                throw ScaModuleException.builder()
                              .errorCode(SCA_OPERATION_VALIDATION_INVALID)
                              .devMsg("Missing selected sca method.")
                              .build();
            }
            scaOperation.setScaMethodId(data.getScaUserDataId());
        }

        if (CollectionUtils.isEmpty(user.getScaUserData())) {
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_VALIDATION_INVALID)
                          .devMsg(String.format("User with login %s has no sca data", user.getLogin()))
                          .build();
        }
    }

    private void checkOperationAttempts(SCAOperationEntity operation, boolean isLoginOperation) {
        int max = isLoginOperation
                          ? loginFailedMax
                          : authCodeFailedMax;
        if (operation.getFailledCount() >= max) {
            throw ScaModuleException.buildAttemptsException(0, isLoginOperation);
        }
    }
}
