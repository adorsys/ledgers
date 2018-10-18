package de.adorsys.ledgers.postings.service.impl;

import de.adorsys.ledgers.postings.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.converter.LedgerMapper;
import de.adorsys.ledgers.postings.domain.*;
import de.adorsys.ledgers.postings.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.postings.service.LedgerService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ITLedgerServiceImplTest {
    private static final String LEDGER_ID = "Ledger Id";
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String NAME = "Mr. Jones";

    private static final LedgerMapper ledgerMapper = Mappers.getMapper(LedgerMapper.class);
    private static final LedgerAccountMapper ledgerAccountMapper = Mappers.getMapper(LedgerAccountMapper.class);

    @InjectMocks
    private LedgerService ledgerService = new LedgerServiceImpl(ledgerMapper);

    @Mock
    private ChartOfAccountRepository chartOfAccountRepository;
    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;
    @Mock
    private Principal principal;

    @Test
    public void newLedger() throws LedgerNotFoundException, ChartOfAccountNotFoundException {
        when(ledgerRepository.save(any())).thenReturn(getLedger());
        when(chartOfAccountRepository.findById(any())).thenReturn(Optional.of(getChartOfAccount()));
        when(principal.getName()).thenReturn(NAME);
        //When
        LedgerBO result = ledgerService.newLedger(ledgerMapper.toLedgerBO(getLedger()));
        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(getLedger());
    }

    @Test
    public void findLedgerById() {
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        //When
        Optional<LedgerBO> result = ledgerService.findLedgerById(LEDGER_ID);
        //Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(getLedger());
    }

    @Test
    public void findLedgerByName() {
        when(ledgerRepository.findOptionalByName(any())).thenReturn(Optional.of(getLedger()));
        //When
        Optional<LedgerBO> result = ledgerService.findLedgerByName(NAME);
        //Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(getLedger());
    }

    @Test
    public void newLedgerAccount() throws LedgerAccountNotFoundException, LedgerNotFoundException {
        when(ledgerAccountRepository.save(any())).thenReturn(getLedgerAccount());
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(getLedger()));
        //When
        LedgerAccountBO result = ledgerService.newLedgerAccount(ledgerAccountMapper.toLedgerAccountBO(getLedgerAccount()));
        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(getLedgerAccount());
    }

    @Test
    public void findLedgerAccountById() {
        when(ledgerAccountRepository.findById(any())).thenReturn(Optional.of(getLedgerAccount()));
        //When
        Optional<LedgerAccountBO> result = ledgerService.findLedgerAccountById(LEDGER_ID);
        //Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(getLedgerAccount());
    }

    @Test
    public void findLedgerAccount() throws LedgerNotFoundException {
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.of(getLedgerAccount()));
        //When
        Optional<LedgerAccountBO> result = ledgerService.findLedgerAccount(ledgerMapper.toLedgerBO(getLedger()), LEDGER_ID);
        //Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(getLedgerAccount());
    }

    private LedgerAccount getLedgerAccount() {
        return new LedgerAccount(LEDGER_ID, DATE_TIME, "User", "Some short description", "Some long description", NAME, getLedger(), null, getChartOfAccount(), BalanceSide.Cr, AccountCategory.AS);
    }

    private Ledger getLedger() {
        return new Ledger(LEDGER_ID, DATE_TIME, "User", "Some short description", "Some long description", NAME, getChartOfAccount(), DATE_TIME);
    }

    private ChartOfAccount getChartOfAccount() {
        return new ChartOfAccount("id", DATE_TIME, NAME, "Some short description", "Some long description", NAME);
    }
}
