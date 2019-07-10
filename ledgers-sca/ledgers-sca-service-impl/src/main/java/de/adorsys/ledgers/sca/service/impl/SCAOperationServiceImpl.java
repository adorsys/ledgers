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
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.sca.service.impl.mapper.SCAOperationMapper;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.hash.BaseHashItem;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.*;
import static de.adorsys.ledgers.sca.exception.SCAErrorCode.USER_SCA_DATA_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class SCAOperationServiceImpl implements SCAOperationService, InitializingBean {
    private static final String TAN_VALIDATION_ERROR = "Can't validate client TAN";
    private static final String AUTH_CODE_GENERATION_ERROR = "TAN can't be generated";
    private static final String STOLEN_ERROR = "Seems auth code was stolen because it already used in the system";
    private static final String EXPIRATION_ERROR = "Operation is not valid because of expiration";

    private final SCAOperationRepository repository;
    private final AuthCodeGenerator authCodeGenerator;
    private final SCAOperationMapper scaOperationMapper;
    private final List<SCASender> sendersList;
    private Map<ScaMethodTypeBO, SCASender> senders = new HashMap<>();
    private HashGenerator hashGenerator = new HashGeneratorImpl();

    //Use property config instead

    @Value("${sca.authCode.validity.seconds:180}")
    private int authCodeValiditySeconds;

    @Value("${sca.authCode.email.body}")
    private String authCodeEmailBody;

    @Value("${sca.authCode.failed.max:5}")
    private int authCodeFailedMax;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Value("${sca.final.weight:100}")
    private int finalWeight;

    @Override
    public void afterPropertiesSet() {
        if (sendersList != null) {
            sendersList.forEach(s -> this.senders.put(s.getType(), s));
        }
    }

    @Override
    public SCAOperationBO generateAuthCode(AuthCodeDataBO data, UserBO user, ScaStatusBO scaStatus) throws SCAOperationValidationException, SCAMethodNotSupportedException {

        SCAOperationEntity scaOperation = loadOrCreateScaOperation(data, scaStatus);

        // One sca method is set, we do not change it anymore.
        if (scaOperation.getScaMethodId() == null) {
            if (data.getScaUserDataId() == null) {
                throw new SCAOperationValidationException("Missing selected sca method.");
            }
            scaOperation.setScaMethodId(data.getScaUserDataId());
        }

        if (user.getScaUserData() == null) {
            throw new SCAOperationValidationException(String.format("User with login %s has no sca data", user.getLogin()));
        }
            scaOperation.setScaStatus(ScaStatus.valueOf(scaStatus.name()));

        ScaUserDataBO scaUserData = getScaUserData(user.getScaUserData(), scaOperation.getScaMethodId());

        checkMethodSupported(scaUserData);

        String tan = authCodeGenerator.generate();

        BaseHashItem<OperationHashItem> hashItem = new BaseHashItem<>(new OperationHashItem(scaOperation.getId(), scaOperation.getOpId(), data.getOpData(), tan));

        scaOperation = buildSCAOperation(scaOperation, hashItem);

        repository.save(scaOperation);

        String usderMessageTemplate = StringUtils.isBlank(data.getUserMessage())
                                              ? authCodeEmailBody
                                              : data.getUserMessage();
        String message = String.format(usderMessageTemplate, tan);
        senders.get(scaUserData.getScaMethod()).send(scaUserData.getMethodValue(), message);

        return scaOperationMapper.toBO(scaOperation);
    }

    @Override
    public boolean validateAuthCode(String authorisationId, String opId, String opData, String authCode, int scaWeight) throws SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {
        Optional<SCAOperationEntity> operationOptional = repository.findById(authorisationId);

        String authCodeHash = operationOptional
                                      .map(SCAOperationEntity::getAuthCodeHash)
                                      .orElseThrow(SCAOperationNotFoundException::new);

        SCAOperationEntity operation = operationOptional.get();

        checkOperationNotUsed(operation);

        checkOperationNotExpired(operation);

        checkSameOperation(operation, opId);

        String generatedHash = generateHash(operation.getId(), opId, opData, authCode);

        boolean isAuthCodeValid = StringUtils.equals(authCodeHash, generatedHash);

        if (isAuthCodeValid) {
            success(operation, scaWeight);
        } else {
            failed(operation);
        }

        return isAuthCodeValid;
    }

    @Override
    public void processExpiredOperations() {
        List<SCAOperationEntity> operations = repository.findByStatus(AuthCodeStatus.SENT);

        log.info("{} operations with status NEW were found", operations.size());

        List<SCAOperationEntity> expiredOperations = operations
                                                             .stream()
                                                             .filter(this::isOperationAlreadyExpired)
                                                             .collect(Collectors.toList());

        expiredOperations.forEach(o -> updateOperationStatus(o, AuthCodeStatus.EXPIRED, o.getScaStatus(), 0));

        log.info("{} operations was detected as EXPIRED", expiredOperations.size());

        repository.saveAll(expiredOperations);

        log.info("Expired operations were updated");
    }

    @Override
    public SCAOperationBO createAuthCode(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus) {
        return scaOperationMapper.toBO(createAuthCodeInternal(authCodeData, scaStatus));
    }

    @Override
    public SCAOperationBO loadAuthCode(String authorizationId) throws SCAOperationNotFoundException {
        SCAOperationEntity o = repository.findById(authorizationId).orElseThrow(() -> new SCAOperationNotFoundException(authorizationId));
        return scaOperationMapper.toBO(o);
    }

    @Override
    public boolean authenticationCompleted(String opId, OpTypeBO opType) {
        List<SCAOperationEntity> found = repository.findByOpIdAndOpType(opId, OpType.valueOf(opType.name()));
        return multilevelScaEnable
                       ? isMultiLevelScaCompleted(found, opType)
                       : isAnyScaCompleted(found);
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

    private void success(SCAOperationEntity operation, int scaWeight) {
        updateOperationStatus(operation, AuthCodeStatus.VALIDATED, ScaStatus.FINALISED, scaWeight);
        repository.save(operation);
    }

    private void failed(SCAOperationEntity operation) {
        operation.setFailledCount(operation.getFailledCount() + 1);
        operation.setStatus(AuthCodeStatus.FAILED);
        if (operation.getFailledCount() >= authCodeFailedMax) {
            operation.setScaStatus(ScaStatus.FAILED);
        }
        operation.setStatusTime(LocalDateTime.now());
        repository.save(operation);
    }

    private void checkSameOperation(SCAOperationEntity operation, String opId) throws SCAOperationValidationException {
        if (!StringUtils.equals(opId, operation.getOpId())) {
            throw new SCAOperationValidationException("Operation id not matching.");
        }
    }

    private String generateHash(String id, String opId, String opData, String authCode) throws SCAOperationValidationException {
        String hash;
        try {
            hash = hashGenerator.hash(new BaseHashItem<>(new OperationHashItem(id, opId, opData, authCode)));
        } catch (HashGenerationException e) {
            log.error(TAN_VALIDATION_ERROR);
            throw new SCAOperationValidationException(TAN_VALIDATION_ERROR, e);
        }
        return hash;
    }

    private void checkOperationNotExpired(SCAOperationEntity operation) throws SCAOperationExpiredException {
        if (isOperationAlreadyExpired(operation)) {
            updateOperationStatus(operation, AuthCodeStatus.EXPIRED, operation.getScaStatus(), 0);
            repository.save(operation);
            log.error(EXPIRATION_ERROR);
            throw new SCAOperationExpiredException(EXPIRATION_ERROR);
        }
    }

    private void checkOperationNotUsed(SCAOperationEntity operation) throws SCAOperationUsedOrStolenException {
        if (isOperationAlreadyUsed(operation)) {
            log.error(STOLEN_ERROR);
            throw new SCAOperationUsedOrStolenException(STOLEN_ERROR);
        }
    }

    private SCAOperationEntity createAuthCodeInternal(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus) {
        if (authCodeData.getAuthorisationId() == null) {
            throw new ScaUncheckedException("Missing authorization id.");
        }
        SCAOperationEntity scaOp = new SCAOperationEntity();
        scaOp.setId(authCodeData.getAuthorisationId());
        scaOp.setOpId(authCodeData.getOpId());
        scaOp.setOpType(OpType.valueOf(authCodeData.getOpType().name()));
        scaOp.setScaMethodId(authCodeData.getScaUserDataId());
        scaOp.setStatus(AuthCodeStatus.INITIATED);
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

    @NotNull
    private SCAOperationEntity buildSCAOperation(SCAOperationEntity scaOperation, BaseHashItem<OperationHashItem> hashItem) {

        String authCodeHash = generateHashByOpData(hashItem);

        scaOperation.setCreated(LocalDateTime.now());
        int validitySeconds = scaOperation.getValiditySeconds() <= 0
                                      ? authCodeValiditySeconds
                                      : scaOperation.getValiditySeconds();
        scaOperation.setValiditySeconds(validitySeconds);
        scaOperation.setStatus(AuthCodeStatus.SENT);
        scaOperation.setStatusTime(LocalDateTime.now());
        scaOperation.setHashAlg(hashItem.getAlg());
        scaOperation.setAuthCodeHash(authCodeHash);
        return scaOperation;
    }

    private String generateHashByOpData(BaseHashItem<OperationHashItem> hashItem) {
        String authCodeHash;
        try {
            authCodeHash = hashGenerator.hash(hashItem);
        } catch (HashGenerationException e) {
            log.error(AUTH_CODE_GENERATION_ERROR);
            throw new ScaUncheckedException(AUTH_CODE_GENERATION_ERROR, e);
        }
        return authCodeHash;
    }

    private SCAOperationEntity loadOrCreateScaOperation(AuthCodeDataBO data, ScaStatusBO scaStatus) {
        if (data.getAuthorisationId() == null) {
            throw new ScaUncheckedException("Missing authorization id.");
        }
        return repository.findById(data.getAuthorisationId()).orElse(createAuthCodeInternal(data, scaStatus));
    }

    private void checkMethodSupported(ScaUserDataBO scaMethod) throws SCAMethodNotSupportedException {
        if (!senders.containsKey(scaMethod.getScaMethod())) {
            throw new SCAMethodNotSupportedException();
        }
    }

    private boolean isOperationAlreadyUsed(SCAOperationEntity operation) {
        return operation.getStatus() == AuthCodeStatus.VALIDATED ||
                       operation.getStatus() == AuthCodeStatus.EXPIRED ||
                       operation.getStatus() == AuthCodeStatus.DONE ||
                       operation.getScaStatus() == ScaStatus.FAILED ||
                       operation.getScaStatus() == ScaStatus.FINALISED;
    }

    private boolean isOperationAlreadyExpired(SCAOperationEntity operation) {
        boolean hasExpiredStatus = operation.getStatus() == AuthCodeStatus.EXPIRED;
        int validitySeconds = operation.getValiditySeconds();
        return hasExpiredStatus || LocalDateTime.now().isAfter(operation.getCreated().plusSeconds(validitySeconds));
    }

    private void updateOperationStatus(SCAOperationEntity operation, AuthCodeStatus status, ScaStatus scaStatus, int scaWeight) {
        operation.setScaWeight(scaWeight);
        operation.setStatus(status);
        operation.setScaStatus(scaStatus);
        operation.setStatusTime(LocalDateTime.now());
    }

    void setHashGenerator(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    void setSenders(Map<ScaMethodTypeBO, SCASender> senders) {
        this.senders = senders;
    }

    void setAuthCodeValiditySeconds(int authCodeValiditySeconds) {
        this.authCodeValiditySeconds = authCodeValiditySeconds;
    }

    void setAuthCodeEmailBody(String authCodeEmailBody) {
        this.authCodeEmailBody = authCodeEmailBody;
    }

    private final static class OperationHashItem {
        @JsonProperty
        private String id;// attach to the database line. Pinning!!!
        @JsonProperty
        private String opId;// attach to the business operation. Pinning!!!
        @JsonProperty
        private String opData;
        @JsonProperty
        private String tan;

        public OperationHashItem(String id, String opId, String opData, String tan) {
            super();
            this.id = id;
            this.opId = opId;
            this.opData = opData;
            this.tan = tan;
        }
    }
}
