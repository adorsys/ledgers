/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.um.db.domain.AisConsentEntity;

public interface AisConsentRepository extends CrudRepository<AisConsentEntity, String> {
}
