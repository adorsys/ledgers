/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Currency;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class AccountReferenceBO {
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private Currency currency;

    @JsonIgnore
    public boolean isInvalidReference() {
        return Stream.of(this.iban, this.bban, this.pan, this.maskedPan, this.msisdn).collect(Collectors.toSet()).size() < 2;
    }
}
