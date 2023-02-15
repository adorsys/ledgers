/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "sca_ais_consent")
public class AisConsentEntity {

	@Id
	private String id;
	
    private String userId;

    private String tppId;

    private int frequencyPerDay;

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_accounts", joinColumns=@JoinColumn(name="ais_consent_entity_id"))
    private List<String> accounts;

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_balances", joinColumns=@JoinColumn(name="ais_consent_entity_id"))
	private List<String> balances;

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_transactions", joinColumns=@JoinColumn(name="ais_consent_entity_id"))
    private List<String> transactions;

    @Enumerated(EnumType.STRING)
    private AisAccountAccessType availableAccounts;

    @Enumerated(EnumType.STRING)
    private AisAccountAccessType allPsd2;
    private LocalDate validUntil;

    private boolean recurringIndicator;
}
