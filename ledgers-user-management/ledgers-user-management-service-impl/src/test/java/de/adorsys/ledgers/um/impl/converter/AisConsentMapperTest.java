/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.util.Ids;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AisConsentMapperTest {

    private AisConsentMapper mapper = Mappers.getMapper(AisConsentMapper.class);

    @Test
    void test_toAisConsentPO() {
        // Given
        AisConsentBO bo = new AisConsentBO();
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        List<String> list = Collections.singletonList("DE80760700240271232400");
        access.setAccounts(list);
        access.setTransactions(list);
        access.setBalances(list);
        bo.setAccess(access);
        bo.setFrequencyPerDay(4);
        bo.setId(Ids.id());
        bo.setRecurringIndicator(true);
        bo.setTppId(Ids.id());
        bo.setUserId(Ids.id());
        bo.setValidUntil(LocalDate.now());

        // When
        AisConsentEntity po = mapper.toAisConsentPO(bo);

        // Then
        assertEquals(bo.getValidUntil(), po.getValidUntil());
        assertEquals(bo.getUserId(), po.getUserId());
        assertEquals(bo.getTppId(), po.getTppId());
        assertEquals(bo.isRecurringIndicator(), po.isRecurringIndicator());
        assertEquals(bo.getId(), po.getId());
        assertEquals(bo.getFrequencyPerDay(), po.getFrequencyPerDay());
        po.getAccounts().containsAll(access.getAccounts());
        po.getTransactions().containsAll(access.getTransactions());
        po.getBalances().containsAll(access.getBalances());
    }
}
