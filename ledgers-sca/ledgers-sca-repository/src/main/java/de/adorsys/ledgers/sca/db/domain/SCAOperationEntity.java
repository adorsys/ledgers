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

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sca_operation")
public class SCAOperationEntity {

    @Id
    @Column(name = "op_id", nullable = false, updatable = false)
    private String opId;

    @Column(name = "validity_seconds", nullable = false, updatable = false)
    private int validitySeconds;

    /* The hash of auth code and opData */
    @Column(name = "auth_code_hash", nullable = false, updatable = false)
    private String authCodeHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthCodeStatus status;

    @Column(name = "hash_alg", nullable = false, updatable = false)
    private String hashAlg;

    @Column(nullable = false, updatable = false)
    private LocalDateTime created;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

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

    @Override
    public String toString() {
        return "SCAOperationEntity{" +
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
