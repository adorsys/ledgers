package de.adorsys.ledgers.sca.domain;

import java.time.LocalDateTime;

/**
 * Documentation for impl.
 * @author fpo
 *
 */
public class SCAOperationBO {
	private String id;
	private String opId;
	private int validitySeconds;
	/*The hash of auth code and opData*/
	private String authCodeHash;
	private AuthCodeStatusBO status;
	private String hashAlg;
	private LocalDateTime created;
	private LocalDateTime statusTime;
	
	private OpTypeBO opType;
	/*
	 * Stores the sca method is associated with this authorization. The sca method
	 * id is not supposed to change after the code was sent out.
	 */
	private String scaMethodId;

	/*
	 * Records the number of failed attempts.
	 * 
	 */
	private int failledCount;
	
	private ScaStatusBO scaStatus;

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

    public AuthCodeStatusBO getStatus() {
        return status;
    }

    public void setStatus(AuthCodeStatusBO status) {
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

    public OpTypeBO getOpType() {
		return opType;
	}

	public void setOpType(OpTypeBO opType) {
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

	public ScaStatusBO getScaStatus() {
		return scaStatus;
	}

	public void setScaStatus(ScaStatusBO scaStatus) {
		this.scaStatus = scaStatus;
	}
}
