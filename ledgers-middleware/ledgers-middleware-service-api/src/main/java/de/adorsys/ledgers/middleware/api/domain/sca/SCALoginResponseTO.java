package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.Data;

@Data
public class SCALoginResponseTO extends SCAResponseTO {
	private String scaId;

	public SCALoginResponseTO() {
		super(SCALoginResponseTO.class.getSimpleName());
	}
}
