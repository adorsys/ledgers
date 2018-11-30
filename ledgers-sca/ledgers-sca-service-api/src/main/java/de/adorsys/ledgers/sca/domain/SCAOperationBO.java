package de.adorsys.ledgers.sca.domain;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        SCAOperationBO that = (SCAOperationBO) o;
        return validitySeconds == that.validitySeconds &&
                       Objects.equals(id, that.id) &&
                       Objects.equals(opId, that.opId) &&
                       Objects.equals(authCodeHash, that.authCodeHash) &&
                       status == that.status &&
                       Objects.equals(hashAlg, that.hashAlg) &&
                       Objects.equals(created, that.created) &&
                       Objects.equals(statusTime, that.statusTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, opId, validitySeconds, authCodeHash, status, hashAlg, created, statusTime);
    }

    @Override
    public String toString() {
        return "SCAOperationBO{" +
                       "id='" + id + '\'' +
                       ", opId='" + opId + '\'' +
                       ", validitySeconds=" + validitySeconds +
                       ", authCodeHash='" + authCodeHash + '\'' +
                       ", status=" + status +
                       ", hashAlg='" + hashAlg + '\'' +
                       ", created=" + created +
                       ", statusTime=" + statusTime +
                       '}';
    }
}
