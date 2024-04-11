/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountReferenceTO {
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private Currency currency;
}
