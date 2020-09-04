package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.Data;

@Data
public class SCAConsentResponseTO extends SCAResponseTO {
    private String consentId;
    private boolean partiallyAuthorised;

    public SCAConsentResponseTO(String consentId) {
        this.consentId = consentId;
    }

    public SCAConsentResponseTO() {
        super(SCAConsentResponseTO.class.getSimpleName());
    }
}
