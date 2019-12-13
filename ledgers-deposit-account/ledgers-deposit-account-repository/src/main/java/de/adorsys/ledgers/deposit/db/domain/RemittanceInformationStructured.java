package de.adorsys.ledgers.deposit.db.domain;

import lombok.Data;

import javax.persistence.Embeddable;

@Data
@Embeddable
public class RemittanceInformationStructured {
    private String reference;
    private String referenceType;
    private String referenceIssuer;
}
