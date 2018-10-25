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
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.exception.AuthCodeGenerationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.util.hash.BaseHashItem;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SCAOperationServiceImpl implements SCAOperationService {
    private static final Logger logger = LoggerFactory.getLogger(SCAOperationServiceImpl.class);
    private static final String TAN_VALIDATION_ERROR = "Can't validate client TAN";
    private static final String AUTH_CODE_GENERATION_ERROR = "TAN can't be generated";

    private final SCAOperationRepository repository;

    private final AuthCodeGenerator authCodeGenerator;

    private HashGenerator hashGenerator;

    public SCAOperationServiceImpl(SCAOperationRepository repository, AuthCodeGenerator authCodeGenerator) {
        this.repository = repository;
        this.authCodeGenerator = authCodeGenerator;
        hashGenerator = new HashGeneratorImpl();
    }

    public void setHashGenerator(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    @Override
    public String generateAuthCode(String opId, String opData, int validitySeconds) throws AuthCodeGenerationException {

        String tan = authCodeGenerator.generate();

        BaseHashItem<OperationHashItem> hashItem = new BaseHashItem<>(new OperationHashItem(opData, tan));

        SCAOperationEntity scaOperation;

        try {
            scaOperation = buildSCAOperation(opId, validitySeconds, hashItem);
        } catch (HashGenerationException e) {
            logger.error(AUTH_CODE_GENERATION_ERROR, e);
            throw new AuthCodeGenerationException(AUTH_CODE_GENERATION_ERROR, e);
        }

        // todo: send tan to the user functionality will be implemented in #107

        repository.save(scaOperation);

        return tan;
    }

    @NotNull
    private SCAOperationEntity buildSCAOperation(String opId, int validitySeconds, BaseHashItem<OperationHashItem> hashItem) throws HashGenerationException {
        String authCodeHash = hashGenerator.hash(hashItem);
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

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public boolean validateAuthCode(String opId, String opData, String authCode) throws SCAOperationNotFoundException, SCAOperationValidationException {

        Optional<SCAOperationEntity> operation = repository.findById(opId);

        String authCodeHash = operation
                                      .map(SCAOperationEntity::getAuthCodeHash)
                                      .orElseThrow(SCAOperationNotFoundException::new);

        String hash;
        try {
            hash = hashGenerator.hash(new BaseHashItem<>(new OperationHashItem(opData, authCode)));
        } catch (HashGenerationException e) {
            logger.error(TAN_VALIDATION_ERROR, e);
            throw new SCAOperationValidationException(TAN_VALIDATION_ERROR, e);
        }

        boolean valid = hash.equals(authCodeHash);

        if (valid) {
            updateOperationStatus(operation.get(), AuthCodeStatus.USED);
        }

        return valid;
    }

    private void updateOperationStatus(SCAOperationEntity operation, AuthCodeStatus status) {
        operation.setStatus(status);
        operation.setStatusTime(LocalDateTime.now());
        repository.save(operation);
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