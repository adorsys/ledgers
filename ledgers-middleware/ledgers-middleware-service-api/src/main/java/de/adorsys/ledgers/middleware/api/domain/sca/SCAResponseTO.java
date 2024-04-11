/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class SCAResponseTO implements AspspConsentDataSerial {
	private ScaStatusTO scaStatus;
	private String authorisationId;
	private List<ScaUserDataTO> scaMethods;
	private ScaUserDataTO chosenScaMethod;
	private ChallengeDataTO challengeData;
	private String psuMessage;
    private LocalDateTime statusDate;
    private int expiresInSeconds;
	private boolean multilevelScaRequired;
	private String authConfirmationCode;

    /*
     * Might be returned as result of an exemption. Meaning that
     * the requested operation has been executed.
     */
    private BearerTokenTO bearerToken;

	private String objectType;

	protected SCAResponseTO(String objectType) {
		this.objectType = objectType;
	}

	@Override
	public String getObjectType() {
		return objectType;
	}
}
