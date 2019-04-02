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

package de.adorsys.ledgers.sca.db.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

/**
 * The SCA operation entity. We distinguish among following business operations.
 * - Login : - the opId shall be a generated login session id. - the
 * authorisationId shall be the same as the operation id. - Consent: A single
 * consent might need multiple authorisations. - the opId is the consentId - the
 * authorisationId is a generated number. - Payment: A single payment might need
 * multiple authorisations. - the opId is the paymentId - the authorisationId is
 * a generated number.
 * 
 * We can create an SCA without have sent out the code. This is generally the
 * case when a business operation (login, consent, payment) is initiated.
 * 
 * @author fpo
 *
 */
@Entity
@Table(name = "sca_operation")
public class SCAOperationEntity {

	/**
	 * The id of this authorization instance. This will generally match the
	 * authorization id.
	 */
	@Id
	private String id;

	/**
	 * The id of the business operation being authorized.
	 */
	@Column(name = "op_id", nullable = false, updatable = false)
	private String opId;

	@Column(name = "validity_seconds", nullable = false, updatable = false)
	private int validitySeconds;

	/* The hash of auth code and opData */
	@Column(name = "auth_code_hash")
	private String authCodeHash;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AuthCodeStatus status;

	@Column(name = "hash_alg")
	private String hashAlg;

	@Column(nullable = false, updatable = false)
	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime created;

	@Column(name = "status_time", nullable = false)
	@Convert(converter = LocalDateTimeConverter.class)
	private LocalDateTime statusTime;

	/* The operation type. Could be login, consent, payment. */
	@Column(name = "op_type")
	@Enumerated(EnumType.STRING)
	private OpType opType;

	/*
	 * Stores the sca method is associated with this authorization. The sca method
	 * id is not supposed to change after the code was sent out.
	 */
	@Column(name = "sca_method_id")
	private String scaMethodId;
	
	/*
	 * Records the number of failed attempts.
	 * 
	 */
	@Column(name = "failled_count")
	private int failledCount;

	@Column(name = "sca_status")
	@Enumerated(EnumType.STRING)
	private ScaStatus scaStatus;

	@Column(nullable = false)
	private int scaWeight;
	
	@PrePersist
	public void prePersist() {
		if(created==null) {
			created = LocalDateTime.now();
		}
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}

	public int getValiditySeconds() {
		return validitySeconds;
	}

	public void setValiditySeconds(int validitySeconds) {
		this.validitySeconds = validitySeconds;
	}

	public String getAuthCodeHash() {
		return authCodeHash;
	}

	public void setAuthCodeHash(String authCodeHash) {
		this.authCodeHash = authCodeHash;
	}

	public AuthCodeStatus getStatus() {
		return status;
	}

	public void setStatus(AuthCodeStatus status) {
		this.status = status;
	}

	public String getHashAlg() {
		return hashAlg;
	}

	public void setHashAlg(String hashAlg) {
		this.hashAlg = hashAlg;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public LocalDateTime getStatusTime() {
		return statusTime;
	}

	public void setStatusTime(LocalDateTime statusTime) {
		this.statusTime = statusTime;
	}

	public OpType getOpType() {
		return opType;
	}

	public void setOpType(OpType opType) {
		this.opType = opType;
	}

	public String getScaMethodId() {
		return scaMethodId;
	}

	public void setScaMethodId(String scaMethodId) {
		this.scaMethodId = scaMethodId;
	}

	public int getFailledCount() {
		return failledCount;
	}

	public void setFailledCount(int failledCount) {
		this.failledCount = failledCount;
	}

	public ScaStatus getScaStatus() {
		return scaStatus;
	}

	public void setScaStatus(ScaStatus scaStatus) {
		this.scaStatus = scaStatus;
	}

	public int getScaWeight() {
		return scaWeight;
	}

	public void setScaWeight(int scaWeight) {
		this.scaWeight = scaWeight;
	}
}
