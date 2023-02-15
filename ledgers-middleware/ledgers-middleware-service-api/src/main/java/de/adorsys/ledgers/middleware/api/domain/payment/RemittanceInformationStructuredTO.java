/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.payment;

import lombok.Data;

@Data
public class RemittanceInformationStructuredTO {
    private String reference;
    private String referenceType;
    private String referenceIssuer;
}
