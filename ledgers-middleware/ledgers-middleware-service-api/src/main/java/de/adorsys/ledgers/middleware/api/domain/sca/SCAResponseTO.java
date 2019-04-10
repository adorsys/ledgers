package de.adorsys.ledgers.middleware.api.domain.sca;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;

public abstract class SCAResponseTO implements AspspConsentDataSerial {
	private ScaStatusTO scaStatus;
	private String authorisationId;
	private List<ScaUserDataTO> scaMethods = new ArrayList<>();
	private ScaUserDataTO chosenScaMethod;
	private ChallengeDataTO challengeData;
	private String psuMessage;
    private LocalDateTime statusDate;
    private int expiresInSeconds;
	private boolean multilevelScaRequired;

    /*
     * Might be returned as result of an exemption. Meaning that
     * the requested operation has been executed.
     */
    private BearerTokenTO bearerToken;
    
    private String objectType;
	private boolean partiallyAuthorised;

	public SCAResponseTO(String objectType) {
		this.objectType = objectType;
	}
	public ScaStatusTO getScaStatus() {
		return scaStatus;
	}
	public void setScaStatus(ScaStatusTO scaStatus) {
		this.scaStatus = scaStatus;
	}
	public String getAuthorisationId() {
		return authorisationId;
	}
	public void setAuthorisationId(String authorisationId) {
		this.authorisationId = authorisationId;
	}
	public List<ScaUserDataTO> getScaMethods() {
		return scaMethods;
	}
	public void setScaMethods(List<ScaUserDataTO> scaMethods) {
		this.scaMethods = scaMethods;
	}
	public ScaUserDataTO getChosenScaMethod() {
		return chosenScaMethod;
	}
	public void setChosenScaMethod(ScaUserDataTO chosenScaMethod) {
		this.chosenScaMethod = chosenScaMethod;
	}

	public String getPsuMessage() {
		return psuMessage;
	}
	public void setPsuMessage(String psuMessage) {
		this.psuMessage = psuMessage;
	}
	public LocalDateTime getStatusDate() {
		return statusDate;
	}
	public void setStatusDate(LocalDateTime statusDate) {
		this.statusDate = statusDate;
	}
	public int getExpiresInSeconds() {
		return expiresInSeconds;
	}
	public void setExpiresInSeconds(int expiresInSeconds) {
		this.expiresInSeconds = expiresInSeconds;
	}
	public BearerTokenTO getBearerToken() {
		return bearerToken;
	}
	public void setBearerToken(BearerTokenTO bearerToken) {
		this.bearerToken = bearerToken;
	}
	@Override
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public ChallengeDataTO getChallengeData() {
		return challengeData;
	}
	public void setChallengeData(ChallengeDataTO challengeData) {
		this.challengeData = challengeData;
	}

	public boolean isMultilevelScaRequired() {
		return multilevelScaRequired;
	}

	public void setMultilevelScaRequired(boolean multilevelScaRequired) {
		this.multilevelScaRequired = multilevelScaRequired;
	}

	public boolean isPartiallyAuthorised() {
		return partiallyAuthorised;
	}

	public void setPartiallyAuthorised(boolean partiallyAuthorised) {
		this.partiallyAuthorised = partiallyAuthorised;
	}
}
