package de.adorsys.ledgers.middleware.api.domain.sca;

public class SCALoginResponseTO extends SCAResponseTO {
	private String scaId;

	public SCALoginResponseTO() {
		super("SCALoginResponseTO");
	}

	public String getScaId() {
		return scaId;
	}

	public void setScaId(String scaId) {
		this.scaId = scaId;
	}
}
