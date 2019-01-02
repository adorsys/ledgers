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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
import de.adorsys.ledgers.sca.exception.SCAMethodNotSupportedException;
import de.adorsys.ledgers.sca.exception.SCAOperationExpiredException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationUsedOrStolenException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.exception.ScaUncheckedException;
import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.sca.service.impl.mapper.SCAOperationMapper;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.hash.BaseHashItem;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import de.adorsys.ledgers.util.hash.HashGenerator;
import de.adorsys.ledgers.util.hash.HashGeneratorImpl;

@Service
public class SCAOperationServiceImpl implements SCAOperationService {
    private static final Logger logger = LoggerFactory.getLogger(SCAOperationServiceImpl.class);
    private static final String TAN_VALIDATION_ERROR = "Can't validate client TAN";
    private static final String AUTH_CODE_GENERATION_ERROR = "TAN can't be generated";
    private static final String STOLEN_ERROR = "Seems auth code was stolen because it already used in the system";
    private static final String EXPIRATION_ERROR = "Operation is not valid because of expiration";

    private final SCAOperationRepository repository;

    private final AuthCodeGenerator authCodeGenerator;
    
    private final SCAOperationMapper scaOperationMapper;

    private HashGenerator hashGenerator;

    private Map<ScaMethodTypeBO, SCASender> senders = new HashMap<>();

    @Value("${sca.authCode.validity.seconds:180}")
    private int authCodeValiditySeconds;

    @Value("${sca.authCode.email.body}")
    private String authCodeEmailBody;

    @Value("${sca.authCode.failed.max:5}")
    private int authCodeFailedMax;
    
    public SCAOperationServiceImpl(List<SCASender> senders, SCAOperationRepository repository, 
    		AuthCodeGenerator authCodeGenerator, SCAOperationMapper scaOperationMapper) {
        this.repository = repository;
        this.scaOperationMapper = scaOperationMapper;
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

    void setAuthCodeValiditySeconds(int authCodeValiditySeconds) {
        this.authCodeValiditySeconds = authCodeValiditySeconds;
    }

    void setAuthCodeEmailBody(String authCodeEmailBody) {
        this.authCodeEmailBody = authCodeEmailBody;
    }

    @Override
    public SCAOperationBO generateAuthCode(AuthCodeDataBO data, UserBO user, ScaStatusBO scaStatus) throws SCAOperationValidationException, SCAMethodNotSupportedException, UserScaDataNotFoundException, SCAOperationNotFoundException {

    	SCAOperationEntity scaOperation;
    	if(data.getAuthorisationId()!=null) {
    		String authorisationId = data.getAuthorisationId();
    		scaOperation = repository.findById(data.getAuthorisationId()).orElseThrow(() -> new SCAOperationNotFoundException(authorisationId));
    	} else {
    		scaOperation = createAuthCodeInternal(data, scaStatus);
    	}
    	
    	// One sca method is set, we do not change it anymore.
    	if(scaOperation.getScaMethodId()==null) {
    		if(data.getScaUserDataId()==null) {
    			throw new SCAOperationValidationException("Missing selected sca method.");
    		}
    		scaOperation.setScaMethodId(data.getScaUserDataId());
    	}
    	
    	if(user.getScaUserData()==null) {
			throw new SCAOperationValidationException(String.format("User with login %s has no sca data", user.getLogin()));
    	}
    	
    	if(scaStatus!=null) {
    		scaOperation.setScaStatus(ScaStatus.valueOf(scaStatus.name()));
    	}
    	
        ScaUserDataBO scaUserData = getScaUserData(user.getScaUserData(), scaOperation.getScaMethodId());

        checkMethodSupported(scaUserData);

        String tan = authCodeGenerator.generate();
        
        BaseHashItem<OperationHashItem> hashItem = new BaseHashItem<>(new OperationHashItem(scaOperation.getId(),scaOperation.getOpId(), data.getOpData(), tan));

        scaOperation = buildSCAOperation(scaOperation, hashItem);

        repository.save(scaOperation);

        String usderMessageTemplate = StringUtils.isBlank(data.getUserMessage())
        		? authCodeEmailBody
        				:data.getUserMessage();
        String message =  String.format(usderMessageTemplate,tan);

        senders.get(scaUserData.getScaMethod()).send(scaUserData.getMethodValue(), message);

        return scaOperationMapper.toBO(scaOperation);
    }
    
    private void checkMethodSupported(ScaUserDataBO scaMethod) throws SCAMethodNotSupportedException {
        if (!senders.containsKey(scaMethod.getScaMethod())) {
            throw new SCAMethodNotSupportedException();
        }
    }

    @NotNull
    private ScaUserDataBO getScaUserData(@NotNull List<ScaUserDataBO> scaUserData, @NotNull String scaUserDataId) throws UserScaDataNotFoundException {
        return scaUserData.stream().filter(s -> scaUserDataId.equals(s.getId())).findFirst().orElseThrow(() -> new UserScaDataNotFoundException(scaUserDataId));
    }

    @NotNull
    private SCAOperationEntity buildSCAOperation(SCAOperationEntity scaOperation, BaseHashItem<OperationHashItem> hashItem) {

        String authCodeHash = generateHashByOpData(hashItem);

        scaOperation.setCreated(LocalDateTime.now());
        int validitySeconds = scaOperation.getValiditySeconds()<=0
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
            logger.error(AUTH_CODE_GENERATION_ERROR, e);
            throw new ScaUncheckedException(AUTH_CODE_GENERATION_ERROR, e);
        }
        return authCodeHash;
    }

    @Override
    public boolean validateAuthCode(String authorisationId, String opId, String opData, String authCode) throws SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException {
    	Optional<SCAOperationEntity> operationOptional = repository.findById(authorisationId);

        String authCodeHash = operationOptional
                                      .map(SCAOperationEntity::getAuthCodeHash)
                                      .orElseThrow(SCAOperationNotFoundException::new);

        SCAOperationEntity operation = operationOptional.get();

        checkOperationNotUsed(operation);

        checkOperationNotExpired(operation);
        
        checkSameOperation(operation, opId);

        String generatedHash = generateHash(operation.getId(), opId, opData, authCode);

        boolean isAuthCodeValid = generatedHash!=null && generatedHash.equals(authCodeHash);

        if (isAuthCodeValid) {
        	success(operation);
        } else {
        	failled(operation);
        }

        return isAuthCodeValid;
    }

	private void success(SCAOperationEntity operation) {
        updateOperationStatus(operation, AuthCodeStatus.VALIDATED, ScaStatus.FINALISED);
        repository.save(operation);
	}

	private SCAOperationEntity failled(SCAOperationEntity operation){
    	operation.setFailledCount(operation.getFailledCount() +1);
    	operation.setStatus(AuthCodeStatus.FAILED);
		if(operation.getFailledCount()>=authCodeFailedMax) {
	    	operation.setScaStatus(ScaStatus.FAILED);
		}
        operation.setStatusTime(LocalDateTime.now());
        return repository.save(operation);
	}    
    
    private void checkSameOperation(SCAOperationEntity operation, String opId) throws SCAOperationValidationException {
    	if(!StringUtils.equals(opId, operation.getOpId())) {
            throw new SCAOperationValidationException("Operation id not matching.");	
    	}
	}

	private String generateHash(String id, String opId, String opData, String authCode) throws SCAOperationValidationException {
        String hash;
        try {
            hash = hashGenerator.hash(new BaseHashItem<>(new OperationHashItem(id, opId, opData, authCode)));
        } catch (HashGenerationException e) {
            logger.error(TAN_VALIDATION_ERROR, e);
            throw new SCAOperationValidationException(TAN_VALIDATION_ERROR, e);
        }
        return hash;
    }

    private void checkOperationNotExpired(SCAOperationEntity operation) throws SCAOperationExpiredException {
        if (isOperationAlreadyExpired(operation)) {
            updateOperationStatus(operation, AuthCodeStatus.EXPIRED, operation.getScaStatus());
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
        List<SCAOperationEntity> operations = repository.findByStatus(AuthCodeStatus.SENT);

        logger.info("{} operations with status NEW were found", operations.size());

        List<SCAOperationEntity> expiredOperations = operations
                                                             .stream()
                                                             .filter(this::isOperationAlreadyExpired)
                                                             .collect(Collectors.toList());

        expiredOperations.forEach(o -> updateOperationStatus(o, AuthCodeStatus.EXPIRED, o.getScaStatus()));

        logger.info("{} operations was detected as EXPIRED", expiredOperations.size());

        repository.saveAll(expiredOperations);

        logger.info("Expired operations were updated");
    }
    
	@Override
	public SCAOperationBO createAuthCode(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus) {
        return scaOperationMapper.toBO(createAuthCodeInternal(authCodeData, scaStatus));
	}

	private SCAOperationEntity createAuthCodeInternal(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus){
		if(authCodeData.getAuthorisationId()==null) {
			authCodeData.setAuthorisationId(Ids.id());
		}
		SCAOperationEntity scaOp = new SCAOperationEntity();
		scaOp.setId(authCodeData.getAuthorisationId());
		scaOp.setOpId(authCodeData.getOpId());
		scaOp.setOpType(OpType.valueOf(authCodeData.getOpType().name()));
		scaOp.setScaMethodId(authCodeData.getScaUserDataId());
		scaOp.setStatus(AuthCodeStatus.INITIATED);
		scaOp.setStatusTime(LocalDateTime.now());
        int validitySeconds = authCodeData.getValiditySeconds()<=0
        		? authCodeValiditySeconds
        		: authCodeData.getValiditySeconds();
		scaOp.setValiditySeconds(validitySeconds);
		scaOp.setScaStatus(ScaStatus.valueOf(scaStatus.name()));
        return repository.save(scaOp);
	}
	
	@Override
	public SCAOperationBO loadAuthCode(String authorizationId) throws SCAOperationNotFoundException {
		SCAOperationEntity o = repository.findById(authorizationId).orElseThrow(() -> new SCAOperationNotFoundException(authorizationId));
		return scaOperationMapper.toBO(o);
	}
	
	@Override
	public boolean authenticationCompleted(String opId, OpTypeBO opType) {
		List<SCAOperationEntity> found = repository.findByOpIdAndOpType(opId, OpType.valueOf(opType.name()));
		// We return false here.
		if(found.isEmpty()) {
			return false;
		}
		for (SCAOperationEntity o : found) {
			if(!AuthCodeStatus.VALIDATED.equals(o.getStatus())) {
				return false;
			}
		}
		return true;
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

    private void updateOperationStatus(SCAOperationEntity operation, AuthCodeStatus status, ScaStatus scaStatus) {
        operation.setStatus(status);
        operation.setScaStatus(scaStatus);
        operation.setStatusTime(LocalDateTime.now());
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