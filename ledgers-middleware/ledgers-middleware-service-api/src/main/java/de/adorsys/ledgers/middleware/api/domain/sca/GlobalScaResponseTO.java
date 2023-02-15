/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GlobalScaResponseTO implements AspspConsentDataSerial {
    private OpTypeTO opType;
    private String operationObjectId;
    private String externalId;
    private String authorisationId;
    private ScaStatusTO scaStatus;
    private List<ScaUserDataTO> scaMethods;
    private ChallengeDataTO challengeData;
    private String psuMessage;
    private LocalDateTime statusDate;
    private int expiresInSeconds;
    private boolean multilevelScaRequired;
    private String authConfirmationCode;
    private String tan;
    private boolean partiallyAuthorised;
    private BearerTokenTO bearerToken;
    private String objectType;
    private TransactionStatusTO transactionStatus;

    public GlobalScaResponseTO(BearerTokenTO bearerToken) {
        this.bearerToken = bearerToken;
    }

    public GlobalScaResponseTO() {
    }

    public GlobalScaResponseTO(OpTypeTO opType, String authorisationId, BearerTokenTO token, ScaStatusTO scaStatus, boolean multilevelScaRequired, List<ScaUserDataTO> scaMethods, String psuMessage) {
        this.opType = opType;
        this.authorisationId = authorisationId;
        this.bearerToken = token;
        this.scaStatus = scaStatus;
        this.multilevelScaRequired = multilevelScaRequired;
        this.scaMethods = scaMethods;
        this.psuMessage = psuMessage;
    }

    public GlobalScaResponseTO(OpTypeTO opType, BearerTokenTO token, ScaStatusTO scaStatus, List<ScaUserDataTO> scaMethods, String psuMessage) {
        this.opType = opType;
        this.bearerToken = token;
        this.scaStatus = scaStatus;
        this.scaMethods = scaMethods;
        this.psuMessage = psuMessage;
        this.statusDate = LocalDateTime.now();
    }

    @Override
    public String getObjectType() {
        return this.getClass().getSimpleName();
    }
}
