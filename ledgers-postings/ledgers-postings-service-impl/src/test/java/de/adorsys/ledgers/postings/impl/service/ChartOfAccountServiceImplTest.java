/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.impl.converter.ChartOfAccountMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ChartOfAccountServiceImplTest {
    private static final String USER_NAME = "TestName";
    private static final ChartOfAccountMapper mapper = Mappers.getMapper(ChartOfAccountMapper.class);

    @InjectMocks
    private ChartOfAccountServiceImpl chartOfAccountService;
    @Mock
    private ChartOfAccountRepository chartOfAccountRepo;

    @Test
    void new_coa_must_produce_id_created_user_and_copy_name_shortdesc_longdesc() {
        // Given
        ChartOfAccount coa = new ChartOfAccount("id", LocalDateTime.now(), "TestName", "shortDesc", "longDesc", "coaName");
        when(chartOfAccountRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ChartOfAccountBO chartOfAccountBO = mapper.toChartOfAccountBO(coa);
        ChartOfAccountBO result = chartOfAccountService.newChartOfAccount(chartOfAccountBO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCreated());
        assertEquals(USER_NAME, result.getUserDetails());
        assertEquals(coa.getName(), result.getName());
        assertEquals(coa.getShortDesc(), result.getShortDesc());
        assertEquals(coa.getLongDesc(), result.getLongDesc());
    }
}
