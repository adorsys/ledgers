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
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.hash.BaseHashItem;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SCAOperationServiceImpl implements SCAOperationService {
    private static final Logger logger = LoggerFactory.getLogger(SCAOperationServiceImpl.class);
    private static final String TAN_VALIDATION_ERROR = "Can't validate client TAN";
    private static final String AUTH_CODE_GENERATION_ERROR = "TAN can't be generated";
    private static final String STOLEN_ERROR = "Seems auth code was stolen because it already used in the system";
    private static final String EXPIRATION_ERROR = "Operation is not valid because of expiration";

    private final SCAOperationRepository repository;

    private final UserService userService;

    private final AuthCodeGenerator authCodeGenerator;

    private HashGenerator hashGenerator;

    private Map<ScaMethodTypeBO, SCASender> senders = new HashMap<>();

    public SCAOperationServiceImpl(List<SCASender> senders, SCAOperationRepository repository, UserService userService, AuthCodeGenerator authCodeGenerator) {
        this.repository = repository;
        this.userService = userService;
        this.authCodeGenerator = authCodeGenerator;
        hashGenerator = new HashGeneratorImpl();
        if (senders != null) {
            senders.forEach(s -> {
                this.senders.put(s.getType(), s);
            });
        }
    }

    void setHashGenerator(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    void setSenders(Map<ScaMethodTypeBO, SCASender> senders) {
        this.senders = senders;
    }

    @Override
    public String generateAuthCode(String userLogin, String scaUserDataId, String paymentId, String opData, String userMessage, int validitySeconds) throws AuthCodeGenerationException, SCAMethodNotSupportedException, UserNotFoundException, UserScaDataNotFoundException {

        // TODO: check the method supports by user

        ScaUserDataBO scaUserData = getScaUserData(userLogin, scaUserDataId);

        checkMethodSupported(scaUserData);

        String tan = authCodeGenerator.generate();

        BaseHashItem<OperationHashItem> hashItem = new BaseHashItem<>(new OperationHashItem(opData, tan));

        SCAOperationEntity scaOperation = buildSCAOperation(paymentId, validitySeconds, hashItem);

        repository.save(scaOperation);

        String message = userMessage + " " + tan;

        senders.get(scaUserData.getScaMethod()).send(scaUserData.getMethodValue(), message);

        return scaOperation.getOpId();
    }

    private void checkMethodSupported(ScaUserDataBO scaMethod) throws SCAMethodNotSupportedException {
        if (!senders.containsKey(scaMethod.getScaMethod())) {
            throw new SCAMethodNotSupportedException();
        }
    }

    @NotNull
    private ScaUserDataBO getScaUserData(String userLogin, String scaUserDataId) throws UserNotFoundException, UserScaDataNotFoundException {
        UserBO userServiceByLogin = userService.findByLogin(userLogin);
        Optional<ScaUserDataBO> scaUserData = userServiceByLogin.getScaUserData()
                                                      .stream()
                                                      .filter(s -> s.getId().equals(scaUserDataId))
                                                      .findFirst();

        scaUserData.orElseThrow(() -> new UserScaDataNotFoundException(scaUserDataId));
        return scaUserData.get();
    }

    @NotNull
    private SCAOperationEntity buildSCAOperation(String opId, int validitySeconds, BaseHashItem<OperationHashItem> hashItem) throws AuthCodeGenerationException {

        String authCodeHash = generateHashByOpData(hashItem);

        SCAOperationEntity scaOperation = new SCAOperationEntity();
        scaOperation.setOpId(opId);
        scaOperation.setCreated(LocalDateTime.now());
        scaOperation.setValiditySeconds(validitySeconds);
        scaOperation.setStatus(AuthCodeStatus.NEW);
        scaOperation.setStatusTime(LocalDateTime.now());
        scaOperation.setHashAlg(hashItem.getAlg());
        scaOperation.setAuthCodeHash(authCodeHash);
        return scaOperation;
    }

    private String generateHashByOpData(BaseHashItem<OperationHashItem> hashItem) throws AuthCodeGenerationException {
        String authCodeHash;
        try {
            authCodeHash = hashGenerator.hash(hashItem);
        } catch (HashGenerationException e) {
            logger.error(AUTH_CODE_GENERATION_ERROR, e);
            throw new AuthCodeGenerationException(AUTH_CODE_GENERATION_ERROR, e);
        }
        return authCodeHash;
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public boolean validateAuthCode(String opId, String opData, String authCode) throws SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {

        Optional<SCAOperationEntity> operationOptional = repository.findById(opId);

        String authCodeHash = operationOptional
                                      .map(SCAOperationEntity::getAuthCodeHash)
                                      .orElseThrow(SCAOperationNotFoundException::new);

        SCAOperationEntity operation = operationOptional.get();

        checkOperationNotUsed(operation);

        checkOperationNotExpired(operation);

        String generatedHash = generateHash(opData, authCode);

        boolean isAuthCodeValid = generatedHash.equals(authCodeHash);

        if (isAuthCodeValid) {
            updateOperationStatus(operation, AuthCodeStatus.USED);
            repository.save(operation);
        }

        return isAuthCodeValid;
    }

    private String generateHash(String opData, String authCode) throws SCAOperationValidationException {
        String hash;
        try {
            hash = hashGenerator.hash(new BaseHashItem<>(new OperationHashItem(opData, authCode)));
        } catch (HashGenerationException e) {
            logger.error(TAN_VALIDATION_ERROR, e);
            throw new SCAOperationValidationException(TAN_VALIDATION_ERROR, e);
        }
        return hash;
    }

    private void checkOperationNotExpired(SCAOperationEntity operation) throws SCAOperationExpiredException {
        if (isOperationAlreadyExpired(operation)) {
            updateOperationStatus(operation, AuthCodeStatus.EXPIRED);
            repository.save(operation);
            logger.error(EXPIRATION_ERROR);
            throw new SCAOperationExpiredException(EXPIRATION_ERROR);
        }
    }

    private void checkOperationNotUsed(SCAOperationEntity operation) throws SCAOperationUsedOrStolenException {
        if (isOperationAlreadyUsed(operation)) {
            logger.error(STOLEN_ERROR);
            throw new SCAOperationUsedOrStolenException(STOLEN_ERROR);
        }
    }

    @Override
    public void processExpiredOperations() {
        List<SCAOperationEntity> operations = repository.findByStatus(AuthCodeStatus.NEW);

        logger.info("{} operations with status NEW were found", operations.size());

        List<SCAOperationEntity> expiredOperations = operations
                                                             .stream()
                                                             .filter(this::isOperationAlreadyExpired)
                                                             .collect(Collectors.toList());

        expiredOperations.forEach(o -> updateOperationStatus(o, AuthCodeStatus.EXPIRED));

        logger.info("{} operations was detected as EXPIRED", expiredOperations.size());

        repository.saveAll(expiredOperations);

        logger.info("Expired operations were updated");
    }

    private boolean isOperationAlreadyUsed(SCAOperationEntity operation) {
        return operation.getStatus() == AuthCodeStatus.USED;
    }

    private boolean isOperationAlreadyExpired(SCAOperationEntity operation) {
        boolean hasExpiredStatus = operation.getStatus() == AuthCodeStatus.EXPIRED;
        int validitySeconds = operation.getValiditySeconds();
        return hasExpiredStatus || LocalDateTime.now().isAfter(operation.getCreated().plusSeconds(validitySeconds));
    }

    private void updateOperationStatus(SCAOperationEntity operation, AuthCodeStatus status) {
        operation.setStatus(status);
        operation.setStatusTime(LocalDateTime.now());
    }

    private final static class OperationHashItem {
        @JsonProperty
        private String opData;
        @JsonProperty
        private String tan;

        OperationHashItem(String opData, String tan) {
            this.opData = opData;
            this.tan = tan;
        }
    }
}