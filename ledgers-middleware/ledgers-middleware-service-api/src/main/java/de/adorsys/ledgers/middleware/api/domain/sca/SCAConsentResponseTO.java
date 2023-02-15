/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.util.Ids;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

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

    public SCAConsentResponseTO(BearerTokenTO token, String consentId, String template) {
        this.setBearerToken(token);
        this.consentId = StringUtils.isBlank(consentId) ? Ids.id() : consentId;
        this.setPsuMessage(template);
        this.setScaStatus(ScaStatusTO.EXEMPTED);
        this.setStatusDate(LocalDateTime.now());
    }
}
