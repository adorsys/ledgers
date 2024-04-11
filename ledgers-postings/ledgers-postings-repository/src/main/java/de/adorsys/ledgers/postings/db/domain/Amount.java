/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Embeddable
public class Amount {
	
    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal amount;

}
