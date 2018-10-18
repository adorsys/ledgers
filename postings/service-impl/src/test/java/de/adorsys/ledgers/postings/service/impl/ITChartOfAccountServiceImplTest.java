package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.repository.*;
import de.adorsys.ledgers.postings.service.ChartOfAccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ITChartOfAccountServiceImplTest {
    private static final String USER_NAME = "TestName";
    private static final String COA_NAME = "TestCoA";
    private static final String COA_ID = "zzz-WWW-zzz";
    private static final ChartOfAccount COA = ChartOfAccount.builder().id(COA_ID).name(COA_NAME).created(LocalDateTime.now()).user(USER_NAME).build();
    @InjectMocks
    private ChartOfAccountService chartOfAccountService = new ChartOfAccountServiceImpl();
    @Mock
    private ChartOfAccountRepository chartOfAccountRepo;
    @Mock
    private Principal principal;

    @Test
    public void newChartOfAccount() {
        when(principal.getName()).thenReturn(USER_NAME);
        when(chartOfAccountRepo.save(any())).thenReturn(COA);
        //When
        ChartOfAccount result = chartOfAccountService.newChartOfAccount(COA);
        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(COA);

    }

    @Test
    public void findChartOfAccountsById() {
        when(chartOfAccountRepo.findById(anyString())).thenReturn(Optional.of(COA));
        when(chartOfAccountRepo.save(any())).thenReturn(COA);
        //When
        Optional<ChartOfAccount> result = chartOfAccountService.findChartOfAccountsById(COA_ID);
        //Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(COA);
    }

    @Test
    public void findChartOfAccountsByName() {
        when(chartOfAccountRepo.findOptionalByName(anyString())).thenReturn(Optional.of(COA));
        //When
        Optional<ChartOfAccount> result = chartOfAccountService.findChartOfAccountsByName(COA_NAME);
        //Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(COA);
    }
}
