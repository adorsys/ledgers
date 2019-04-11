package de.adorsys.ledgers.middleware.api.domain.sca;

public class SCAConsentResponseTO extends SCAResponseTO {
	private String consentId;
	private boolean partiallyAuthorised;

	public SCAConsentResponseTO() {
		super(SCAConsentResponseTO.class.getSimpleName());
	}

	public String getConsentId() {
		return consentId;
	}

	public void setConsentId(String consentId) {
		this.consentId = consentId;
	}

	public boolean isPartiallyAuthorised() {
		return partiallyAuthorised;
	}

	public void setPartiallyAuthorised(boolean partiallyAuthorised) {
		this.partiallyAuthorised = partiallyAuthorised;
	}
}
