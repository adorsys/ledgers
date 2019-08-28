package de.adorsys.ledgers.sca.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Documentation for impl.
 */
@Data
public class SCAOperationBO {
    private String id;
    private String opId;
    private int validitySeconds;

    /*
     *The hash of auth code and opData
     */
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
     */
    private int failledCount;
    private ScaStatusBO scaStatus;
    private String tan;
}
