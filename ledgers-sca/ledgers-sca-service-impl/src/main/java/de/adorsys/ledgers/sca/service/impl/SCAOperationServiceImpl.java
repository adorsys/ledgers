/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.sca.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.OpType;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.domain.ScaStatus;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.sca.service.ScaMessageResolver;
import de.adorsys.ledgers.sca.service.impl.mapper.SCAOperationMapper;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import de.adorsys.ledgers.util.hash.BaseHashItem;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.sca.db.domain.AuthCodeStatus.*;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.*;
import static de.adorsys.ledgers.util.exception.SCAErrorCode.*;

@Slf4j
@Service
@Setter
@RequiredArgsConstructor
public class SCAOperationServiceImpl implements SCAOperationService, InitializingBean {//NOPMD //TODO REFACTOR THIS GOD CLASS
    private static final String TAN_VALIDATION_ERROR = "Can't validate client TAN";
    private static final String AUTH_CODE_GENERATION_ERROR = "TAN can't be generated";
    private static final String STOLEN_ERROR = "Seems auth code was stolen because it already used in the system";
    private static final String EXPIRATION_ERROR = "Operation is not valid because of expiration";

    private final Environment env;
    private final SCAOperationRepository repository;
    private final AuthCodeGenerator authCodeGenerator;
    private final SCAOperationMapper scaOperationMapper;
    private final List<SCASender> sendersList;
    private final ScaMessageResolver messageResolver;
    private Map<ScaMethodTypeBO, SCASender> senders = new EnumMap<>(ScaMethodTypeBO.class);
    private HashGenerator hashGenerator = new HashGeneratorImpl();

    //Use property config instead

    @Value("${ledgers.sca.authCode.validity.seconds:600}")
    private int authCodeValiditySeconds;

    @Value("${ledgers.sca.authCode.failed.max:5}")
    private int authCodeFailedMax;

    @Value("${ledgers.sca.login.failed.max:3}")
    private int loginFailedMax;

    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Value("${ledgers.sca.final.weight:100}")
    private int finalWeight;

    @Value("${ledgers.sca.authorisation_confirmation_enabled:false}")
    private boolean authConfirmationEnabled;

    @Override
    public void afterPropertiesSet() {
        if (sendersList != null) {
            sendersList.forEach(s -> this.senders.put(s.getType(), s));
        }
    }

    @Override
    public SCAOperationBO generateAuthCode(AuthCodeDataBO data, UserBO user, ScaStatusBO scaStatus) {
        SCAOperationEntity scaOperation = loadOrCreateScaOperation(data, scaStatus);
        checkOperationAttempts(scaOperation, false);
        checkScaOperationIsValid(data, user, scaOperation);
        scaOperation.setScaStatus(ScaStatus.valueOf(scaStatus.name()));

        ScaUserDataBO scaUserData = getScaUserData(user.getScaUserData(), scaOperation.getScaMethodId());
        checkMethodSupported(scaUserData);
        String tan = getTanDependingOnStrategy(scaUserData);
        BaseHashItem<OperationHashItem> hashItem = new BaseHashItem<>(new OperationHashItem(scaOperation.getId(), scaOperation.getOpId(), tan));
        updateSCAOperation(scaOperation, hashItem);

        repository.save(scaOperation);
        if (scaUserData.getScaMethod()!= ScaMethodTypeBO.EMAIL|| scaUserData.isEmailValid()) {
            ScaMessage message = messageResolver.resolveMessage(data,scaUserData, tan);
            senders.get(scaUserData.getScaMethod()).send(message); //TODO Implement a queue to be able to deliver messages failed for some reason! https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/-/issues/837
        }
        SCAOperationBO scaOperationBO = scaOperationMapper.toBO(scaOperation);
        scaOperationBO.setTan(tan);
        return scaOperationBO;
    }

    @Override
    public ScaValidationBO validateAuthCode(String authorisationId, String opId, String authCode, int scaWeight) {
        SCAOperationEntity operation = repository.findById(authorisationId)
                                               .orElseThrow(() -> ScaModuleException.builder()
                                                                          .errorCode(SCA_OPERATION_NOT_FOUND)
                                                                          .devMsg("Sca operation does not contain SCA DATA")
                                                                          .build());
        String authCodeHash = operation.getAuthCodeHash();
        checkOperationAttempts(operation, false);
        checkOperationNotUsed(operation);
        checkOperationNotExpired(operation);
        checkSameOperation(operation, opId);

        String generatedHash = generateHash(operation.getId(), opId, authCode);
        boolean isAuthCodeValid = StringUtils.equals(authCodeHash, generatedHash);
        ScaValidationBO scaValidation = new ScaValidationBO(isAuthCodeValid);
        if (isAuthCodeValid) {
            success(operation, scaWeight, scaValidation);
        } else {
            throw updateFailedCount(authorisationId, false);
        }
        return scaValidation;
    }

    @Override
    public void processExpiredOperations() {
        List<SCAOperationEntity> operations = repository.findByStatus(SENT);

        log.info("{} operations with status NEW were found", operations.size());

        List<SCAOperationEntity> expiredOperations = operations
                                                             .stream()
                                                             .filter(this::isOperationAlreadyExpired)
                                                             .collect(Collectors.toList());

        expiredOperations.forEach(o -> updateOperationStatus(o, EXPIRED, o.getScaStatus(), 0));

        log.info("{} operations was detected as EXPIRED", expiredOperations.size());

        repository.saveAll(expiredOperations);

        log.info("Expired operations were updated");
    }

    @Override
    public SCAOperationBO createAuthCode(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus) {
        checkAuthIdPresent(authCodeData);
        return scaOperationMapper.toBO(createAuthCodeInternal(authCodeData, scaStatus));
    }

    @Override
    public SCAOperationBO loadAuthCode(String authorizationId) {
        SCAOperationEntity o = repository.findById(authorizationId)
                                       .orElseThrow(() -> ScaModuleException.builder()
                                                                  .errorCode(SCA_OPERATION_NOT_FOUND)
                                                                  .devMsg(String.format("Sca operation for authorization %s not found", authorizationId))
                                                                  .build());
        return scaOperationMapper.toBO(o);
    }

    @Override
    public boolean authenticationCompleted(String opId, OpTypeBO opType) {
        List<SCAOperationEntity> found = repository.findByOpIdAndOpType(opId, OpType.valueOf(opType.name()));
        return multilevelScaEnable
                       ? isMultiLevelScaCompleted(found, opType)
                       : isAnyScaCompleted(found);
    }

    @Override
    public ScaAuthConfirmationBO verifyAuthConfirmationCode(String authorisationId, String confirmationCode) {
        SCAOperationEntity entity = getScaOperationEntityByIdAndUnconfirmed(authorisationId);
        boolean isCodeConfirmValid = StringUtils.equals(entity.getAuthCodeHash(), generateHash(authorisationId, confirmationCode));
        repository.save(entity.updateStatuses(isCodeConfirmValid));
        return new ScaAuthConfirmationBO(isCodeConfirmValid, OpTypeBO.valueOf(entity.getOpType().name()), entity.getOpId());
    }

    @Override
    public ScaAuthConfirmationBO completeAuthConfirmation(String authorisationId, boolean authCodeConfirmed) {
        SCAOperationEntity entity = getScaOperationEntityByIdAndUnconfirmed(authorisationId);
        repository.save(entity.updateStatuses(authCodeConfirmed));
        return new ScaAuthConfirmationBO(authCodeConfirmed, OpTypeBO.valueOf(entity.getOpType().name()), entity.getOpId());
    }

    @Override
    public SCAOperationBO checkIfExistsOrNew(AuthCodeDataBO data) {
        checkAuthIdPresent(data);
        Optional<SCAOperationEntity> scaOperation = repository.findById(data.getAuthorisationId());
        scaOperation.ifPresent(o -> {
            checkOperationAttempts(o, true);
            checkOperationNotExpired(o);
            checkOperationNotUsed(o);
        });
        return scaOperation.map(scaOperationMapper::toBO)
                       .orElseGet(() -> createAuthCode(data, ScaStatusBO.PSUAUTHENTICATED));
    }

    private void checkAuthIdPresent(AuthCodeDataBO data) {
        if (StringUtils.isBlank(data.getAuthorisationId())) {
            throw ScaModuleException.builder()
                          .errorCode(AUTH_CODE_GENERATION_FAILURE)
                          .devMsg("Missing authorization id.")
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

    @Override
    public ScaModuleException updateFailedCount(String authorisationId, boolean isLoginOperation) {
        SCAOperationEntity operationEntity = getScaOperationEntityById(authorisationId);
        failed(operationEntity, isLoginOperation);
        repository.save(operationEntity);
        return ScaModuleException.buildAttemptsException(loginFailedMax - operationEntity.getFailledCount(), isLoginOperation);
    }

    private SCAOperationEntity getScaOperationEntityByIdAndUnconfirmed(String authorisationId) {
        return repository.findByIdAndScaStatus(authorisationId, ScaStatus.UNCONFIRMED)
                       .orElseThrow(() -> ScaModuleException.builder()
                                                  .errorCode(SCA_OPERATION_NOT_FOUND)
                                                  .devMsg(String.format("Sca operation for authorisation %s not found", authorisationId))
                                                  .build());
    }

    private SCAOperationEntity getScaOperationEntityById(String authorisationId) {
        return repository.findById(authorisationId)
                       .orElseThrow(() -> ScaModuleException.builder()
                                                  .errorCode(SCA_OPERATION_NOT_FOUND)
                                                  .devMsg(String.format("Sca operation for authorisation %s not found", authorisationId))
                                                  .build());
    }

    private String getTanDependingOnStrategy(ScaUserDataBO scaUserData) {
        return Arrays.asList(this.env.getActiveProfiles()).contains("sandbox")
                       && scaUserData.isUsesStaticTan()
                       && StringUtils.isNotBlank(scaUserData.getStaticTan())
                       ? scaUserData.getStaticTan()
                       : authCodeGenerator.generate();
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

    private boolean isMultiLevelScaCompleted(List<SCAOperationEntity> found, OpTypeBO opType) {
        return EnumSet.of(PAYMENT, CANCEL_PAYMENT, CONSENT).contains(opType)
                       && isCompletedByAllUsers(found);
    }

    private boolean isCompletedByAllUsers(List<SCAOperationEntity> found) {
        return found.stream()
                       .filter(op -> op.getScaStatus() == ScaStatus.FINALISED)
                       .collect(Collectors.summarizingInt(SCAOperationEntity::getScaWeight))
                       .getSum() >= finalWeight;
    }

    private boolean isAnyScaCompleted(List<SCAOperationEntity> found) {
        return found.stream()
                       .anyMatch(op -> op.getScaStatus() == ScaStatus.FINALISED);
    }

    private void success(SCAOperationEntity operation, int scaWeight, ScaValidationBO scaValidation) {
        ScaStatus status = ScaStatus.FINALISED;
        if (authConfirmationEnabled) {
            status = ScaStatus.UNCONFIRMED;
            String confirmationCode = UUID.randomUUID().toString();
            operation.setAuthCodeHash(generateHash(operation.getId(), confirmationCode));
            scaValidation.setAuthConfirmationCode(confirmationCode);
        }
        scaValidation.setScaStatus(ScaStatusBO.valueOf(status.name()));
        updateOperationStatus(operation, VALIDATED, status, scaWeight);
        repository.save(operation);
    }

    private void failed(SCAOperationEntity operation, boolean isLoginOperation) {
        operation.setFailledCount(operation.getFailledCount() + 1);
        operation.setStatus(FAILED);
        int failedMax = isLoginOperation
                                ? loginFailedMax
                                : authCodeFailedMax;
        if (operation.getFailledCount() >= failedMax) {
            operation.setScaStatus(ScaStatus.FAILED);
        }
        operation.setStatusTime(LocalDateTime.now());
    }

    private void checkSameOperation(SCAOperationEntity operation, String opId) {
        if (!StringUtils.equals(opId, operation.getOpId())) {
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_VALIDATION_INVALID)
                          .devMsg("Operation id not matching.")
                          .build();
        }
    }

    private String generateHash(String id, String confirmationCode) {
        return generateHash(id, null, confirmationCode);
    }

    private String generateHash(String id, String opId, String authCode) {
        String hash;
        try {
            hash = hashGenerator.hash(new BaseHashItem<>(new OperationHashItem(id, opId, authCode)));
        } catch (HashGenerationException e) {
            log.error(TAN_VALIDATION_ERROR);
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_VALIDATION_INVALID)
                          .devMsg(TAN_VALIDATION_ERROR)
                          .build();
        }
        return hash;
    }

    private void checkOperationNotExpired(SCAOperationEntity operation) {
        if (isOperationAlreadyExpired(operation)) {
            updateOperationStatus(operation, EXPIRED, operation.getScaStatus(), 0);
            repository.save(operation);
            log.error(EXPIRATION_ERROR);
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_EXPIRED)
                          .devMsg(EXPIRATION_ERROR)
                          .build();
        }
    }

    private void checkOperationNotUsed(SCAOperationEntity operation) {
        if (isOperationAlreadyUsed(operation)) {
            log.error(STOLEN_ERROR);
            throw ScaModuleException.builder()
                          .errorCode(SCA_OPERATION_USED_OR_STOLEN)
                          .devMsg(STOLEN_ERROR)
                          .build();
        }
    }

    private SCAOperationEntity createAuthCodeInternal(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus) {
        SCAOperationEntity scaOp = new SCAOperationEntity();
        scaOp.setId(authCodeData.getAuthorisationId());
        scaOp.setOpId(authCodeData.getOpId());
        scaOp.setExternalId(authCodeData.getExternalId());
        scaOp.setOpType(OpType.valueOf(authCodeData.getOpType().name()));
        scaOp.setScaMethodId(authCodeData.getScaUserDataId());
        scaOp.setStatus(INITIATED);
        scaOp.setStatusTime(LocalDateTime.now());
        int validitySeconds = authCodeData.getValiditySeconds() <= 0
                                      ? authCodeValiditySeconds
                                      : authCodeData.getValiditySeconds();
        scaOp.setValiditySeconds(validitySeconds);
        scaOp.setScaStatus(ScaStatus.valueOf(scaStatus.name()));
        scaOp.setScaWeight(authCodeData.getScaWeight());
        return repository.save(scaOp);
    }

    @NotNull
    private ScaUserDataBO getScaUserData(@NotNull List<ScaUserDataBO> scaUserData, @NotNull String scaUserDataId) {
        return scaUserData.stream()
                       .filter(s -> scaUserDataId.equals(s.getId()))
                       .findFirst()
                       .orElseThrow(() -> ScaModuleException
                                                  .builder()
                                                  .errorCode(USER_SCA_DATA_NOT_FOUND)
                                                  .devMsg(String.format("Sca data not found for: %s", scaUserDataId))
                                                  .build());
    }

    private void updateSCAOperation(SCAOperationEntity scaOperation, BaseHashItem<OperationHashItem> hashItem) {
        String authCodeHash = generateHashByOpData(hashItem);

        scaOperation.setCreated(LocalDateTime.now());
        int validitySeconds = scaOperation.getValiditySeconds() <= 0
                                      ? authCodeValiditySeconds
                                      : scaOperation.getValiditySeconds();
        scaOperation.setValiditySeconds(validitySeconds);
        scaOperation.setStatus(SENT);
        scaOperation.setStatusTime(LocalDateTime.now());
        scaOperation.setHashAlg(hashItem.getAlg());
        scaOperation.setAuthCodeHash(authCodeHash);
    }

    private String generateHashByOpData(BaseHashItem<OperationHashItem> hashItem) {
        String authCodeHash;
        try {
            authCodeHash = hashGenerator.hash(hashItem);
        } catch (HashGenerationException e) {
            log.error(AUTH_CODE_GENERATION_ERROR);
            throw ScaModuleException.builder()
                          .errorCode(AUTH_CODE_GENERATION_FAILURE)
                          .devMsg(AUTH_CODE_GENERATION_ERROR)
                          .build();
        }
        return authCodeHash;
    }

    private SCAOperationEntity loadOrCreateScaOperation(AuthCodeDataBO data, ScaStatusBO scaStatus) {
        if (data.getAuthorisationId() == null) {
            throw ScaModuleException.builder()
                          .errorCode(AUTH_CODE_GENERATION_FAILURE)
                          .devMsg("Missing authorization id.")
                          .build();
        }
        return repository.findById(data.getAuthorisationId()).orElseGet(() -> createAuthCodeInternal(data, scaStatus));
    }

    private void checkMethodSupported(ScaUserDataBO scaMethod) {
        if (!senders.containsKey(scaMethod.getScaMethod())) {
            throw ScaModuleException.builder()
                          .errorCode(SCA_METHOD_NOT_SUPPORTED)
                          .devMsg(String.format("SCA method %s is not supported", scaMethod.getScaMethod().name()))
                          .build();
        }
    }

    private boolean isOperationAlreadyUsed(SCAOperationEntity operation) {
        return EnumSet.of(VALIDATED, EXPIRED, DONE).contains(operation.getStatus())
                       || EnumSet.of(ScaStatus.FAILED, ScaStatus.FINALISED).contains(operation.getScaStatus());
    }

    private boolean isOperationAlreadyExpired(SCAOperationEntity operation) {
        boolean hasExpiredStatus = operation.getStatus() == EXPIRED;
        int validitySeconds = operation.getValiditySeconds();
        return hasExpiredStatus || LocalDateTime.now().isAfter(operation.getCreated().plusSeconds(validitySeconds));
    }

    private void updateOperationStatus(SCAOperationEntity operation, AuthCodeStatus status, ScaStatus scaStatus, int scaWeight) {
        operation.setScaWeight(scaWeight);
        operation.setStatus(status);
        operation.setScaStatus(scaStatus);
        operation.setStatusTime(LocalDateTime.now());
    }

    private static final class OperationHashItem {
        @JsonProperty
        private String id;// attach to the database line. Pinning!!!
        @JsonProperty
        private String opId;// attach to the business operation. Pinning!!!
        @JsonProperty
        private String tan;

        public OperationHashItem(String id, String opId, String tan) {
            this.id = id;
            this.opId = opId;
            this.tan = tan;
        }
    }
}
