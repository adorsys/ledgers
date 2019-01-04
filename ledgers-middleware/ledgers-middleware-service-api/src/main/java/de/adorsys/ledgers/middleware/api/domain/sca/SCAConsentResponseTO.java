package de.adorsys.ledgers.middleware.api.domain.sca;

public class SCAConsentResponseTO extends SCAResponseTO {
	private String consentId;

	public SCAConsentResponseTO() {
		super(SCAConsentResponseTO.class.getSimpleName());
	}

	public String getConsentId() {
		return consentId;
	}

	public void setConsentId(String consentId) {
		this.consentId = consentId;
	}
}
