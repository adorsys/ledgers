/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.db.domain;

import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Data
@Embeddable
public class AccountReference {

	@Column(nullable=false)
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;

    @Column(nullable = false)
    private String currency;

}
