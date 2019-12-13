package de.adorsys.ledgers.middleware.api.domain.payment;

import lombok.Data;

@Data
public class RemittanceInformationStructuredTO {
    private String reference;
    private String referenceType;
    private String referenceIssuer;
}
