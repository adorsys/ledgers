/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import de.adorsys.ledgers.util.hash.BaseHashItem;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.sca.db.domain.AuthCodeStatus.SENT;
import static de.adorsys.ledgers.util.exception.SCAErrorCode.*;

@Slf4j
@Service
@Setter
@RequiredArgsConstructor
public class SCAOperationServiceImpl implements SCAOperationService, InitializingBean {
    private static final String TAN_GENERATION_ERROR = "Could not generate TAN, Please contact your Bank Support";
    private static final String AUTH_CODE_GENERATION_ERROR = "TAN can't be generated, ERROR: {}";


    private final UserService userService;
    private final Environment env;
    private final SCAOperationRepository repository;
    private final AuthCodeGenerator authCodeGenerator;
    private final SCAOperationMapper scaOperationMapper;
    private final List<SCASender<? extends ScaMessage>> sendersList;
    private final ScaMessageResolver<?> otpMessageResolver;
    private final ScaOperationValidationService validationService;
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
        validationService.checkValidityAndAttempts(scaOperation, data, user);
        scaOperation.setScaStatus(ScaStatus.valueOf(scaStatus.name()));

        ScaUserDataBO scaUserData = getScaUserData(user.getScaUserData(), scaOperation.getScaMethodId());
        checkMethodSupported(scaUserData);
        String tan = getTanDependingOnStrategy(scaUserData);
        String hash = generateHash(scaOperation.getId(), scaOperation.getOpId(), tan);
        scaOperation.updateStatusSent(authCodeValiditySeconds, hash, HashGenerator.DEFAULT_HASH_ALG);

        repository.save(scaOperation);
        if (scaUserData.getScaMethod() != ScaMethodTypeBO.SMTP_OTP || scaUserData.isEmailValid()) {
            ScaMessage userMessage = otpMessageResolver.resolveMessage(data, scaUserData, tan);
            senders.get(scaUserData.getScaMethod()).send(userMessage);
            //TODO Implement a queue to be able to deliver messages failed for some reason! https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/-/issues/837
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
        validationService.checkAll(operation, opId);

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
                                                             .filter(SCAOperationEntity::isOperationExpired)
                                                             .collect(Collectors.toList());

        expiredOperations.forEach(SCAOperationEntity::expireOperation);

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
                       ? validationService.isMultiLevelScaCompleted(found, opType)
                       : validationService.isAnyScaCompleted(found);
    }

    @Override
    public ScaAuthConfirmationBO verifyAuthConfirmationCode(String authorisationId, String confirmationCode) {
        SCAOperationEntity entity = getScaOperationEntityByIdAndUnconfirmed(authorisationId);
        boolean isCodeConfirmValid = StringUtils.equals(entity.getAuthCodeHash(), generateHash(authorisationId, null, confirmationCode));
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
        scaOperation.ifPresent(validationService::checksOnPresentOperation);
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

    @Override
    public ScaModuleException updateFailedCount(String authorisationId, boolean isLoginOperation) {
        SCAOperationEntity operationEntity = getScaOperationEntityById(authorisationId);
        operationEntity.fail(isLoginOperation, loginFailedMax, authCodeFailedMax);
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

    private void success(SCAOperationEntity operation, int scaWeight, ScaValidationBO scaValidation) {
        ScaStatus status = ScaStatus.FINALISED;
        if (authConfirmationEnabled) {
            status = ScaStatus.UNCONFIRMED;
            String confirmationCode = UUID.randomUUID().toString();
            operation.setAuthCodeHash(generateHash(operation.getId(), null, confirmationCode));
            scaValidation.setAuthConfirmationCode(confirmationCode);
        }
        scaValidation.setScaStatus(ScaStatusBO.valueOf(status.name()));
        operation.validate(status, scaWeight);
        repository.save(operation);
    }

    private String generateHash(String id, String opId, String authCode) {
        String hash;
        try {
            hash = hashGenerator.hash(new BaseHashItem<>(new OperationHashItem(id, opId, authCode)));
        } catch (HashGenerationException e) {
            log.error(AUTH_CODE_GENERATION_ERROR, e.getMessage());
            throw ScaModuleException.builder()
                          .errorCode(AUTH_CODE_GENERATION_FAILURE)
                          .devMsg(TAN_GENERATION_ERROR)
                          .build();
        }
        return hash;
    }

    private SCAOperationEntity createAuthCodeInternal(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus) {
        SCAOperationEntity scaOp = new SCAOperationEntity(authCodeData.getAuthorisationId(), authCodeData.getOpId(), authCodeData.getExternalId(),
                                                          OpType.valueOf(authCodeData.getOpType().name()), authCodeData.getScaUserDataId(),
                                                          authCodeData.getValiditySeconds(), authCodeValiditySeconds,
                                                          ScaStatus.valueOf(scaStatus.name()), authCodeData.getScaWeight());
        return repository.save(scaOp);
    }

    @NotNull
    private ScaUserDataBO getScaUserData(@NotNull List<ScaUserDataBO> scaUserData, String scaUserDataId) {
        return scaUserData.stream()
                       .filter(s -> scaUserDataId.equals(s.getId()))
                       .findFirst()
                       .map(this::mapToScaUserDataDecodingTan)
                       .orElseThrow(() -> ScaModuleException
                                                  .builder()
                                                  .errorCode(USER_SCA_DATA_NOT_FOUND)
                                                  .devMsg(String.format("Sca data not found for: %s", scaUserDataId))
                                                  .build());
    }

    private ScaUserDataBO mapToScaUserDataDecodingTan(ScaUserDataBO data) {
        return new ScaUserDataBO(data.getId(), data.getScaMethod(), data.getMethodValue(), data.isUsesStaticTan(),
                                 userService.decodeStaticTan(data.getStaticTan()), data.isValid());
    }

    private SCAOperationEntity loadOrCreateScaOperation(AuthCodeDataBO data, ScaStatusBO scaStatus) {
        if (data.getAuthorisationId() == null) {
            throw ScaModuleException.builder()
                          .errorCode(AUTH_CODE_GENERATION_FAILURE)
                          .devMsg("Missing authorization id.")
                          .build();
        }
        return repository.findById(data.getAuthorisationId())
                       .orElseGet(() -> createAuthCodeInternal(data, scaStatus));
    }

    private void checkMethodSupported(ScaUserDataBO scaMethod) {
        if (!senders.containsKey(scaMethod.getScaMethod())) {
            throw ScaModuleException.builder()
                          .errorCode(SCA_METHOD_NOT_SUPPORTED)
                          .devMsg(String.format("SCA method %s is not supported", scaMethod.getScaMethod().name()))
                          .build();
        }
    }

    public static final class OperationHashItem {
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
