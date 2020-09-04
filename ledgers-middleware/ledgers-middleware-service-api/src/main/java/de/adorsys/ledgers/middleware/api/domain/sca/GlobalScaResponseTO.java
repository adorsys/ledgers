package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GlobalScaResponseTO {
    private OpTypeTO opType;
    private String operationObjectId;
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
}
