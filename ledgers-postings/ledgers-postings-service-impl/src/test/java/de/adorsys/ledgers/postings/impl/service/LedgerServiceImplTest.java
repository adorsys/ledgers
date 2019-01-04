package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.db.domain.*;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.impl.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.impl.converter.LedgerMapper;
import de.adorsys.ledgers.util.Ids;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LedgerServiceImplTest {
    private static final LocalDateTime DATE_TIME = LocalDateTime.now();
    private static final String USER_NAME = "Mr. Jones";
    private static final ChartOfAccount COA = new ChartOfAccount(Ids.id(), DATE_TIME, USER_NAME,
            "Some short description", "Some long description", "COA");
    private static final Ledger LEDGER = new Ledger(Ids.id(), DATE_TIME, USER_NAME, "Some short description",
            "Some long description", "Ledger", COA);
    private static final LedgerAccount LEDGER_ACCOUNT = new LedgerAccount(Ids.id(), DATE_TIME, USER_NAME,
            "Some short description", "Some long description", USER_NAME, LEDGER, null, COA, BalanceSide.Cr,
            AccountCategory.AS);
    private static final LedgerMapper LEDGER_MAPPER = new LedgerMapper();
    private static final LedgerAccountMapper LEDGER_ACCOUNT_MAPPER = new LedgerAccountMapper();
    private static final String SYSTEM = "System";

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    @Mock
    private ChartOfAccountRepository chartOfAccountRepository;
    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;
    @Mock
    private Principal principal;
    @Mock
    private LedgerMapper ledgerMapper;
    @Mock
    private LedgerAccountMapper ledgerAccountMapper;

    @Test
    public void new_ledger_must_produce_id_created_user_copy_other_fields()
            throws ChartOfAccountNotFoundException {
        when(chartOfAccountRepository.findById(COA.getId())).thenReturn(Optional.of(COA));
        when(ledgerRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ledgerMapper.toLedgerBO(any())).thenReturn(LEDGER_MAPPER.toLedgerBO(LEDGER));
        // When
        LedgerBO result = ledgerService.newLedger(LEDGER_MAPPER.toLedgerBO(LEDGER));
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getUserDetails()).isEqualTo(USER_NAME);
        assertThat(result.getName()).isEqualTo(LEDGER.getName());
        assertThat(result.getShortDesc()).isEqualTo(LEDGER.getShortDesc());
        assertThat(result.getLongDesc()).isEqualTo(LEDGER.getLongDesc());

        assertThat(result.getCoa()).isNotNull();
    }

    @Test
    public void new_ledgerAccount_must_produce_id_created_user_copy_other_fields() throws LedgerAccountNotFoundException, LedgerNotFoundException {
        when(ledgerAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ledgerRepository.findById(any())).thenReturn(Optional.of(LEDGER));
        when(ledgerAccountMapper.toLedgerAccountBO(any())).thenReturn(LEDGER_ACCOUNT_MAPPER.toLedgerAccountBO(LEDGER_ACCOUNT));
        // When
        LedgerAccountBO result = ledgerService.newLedgerAccount(LEDGER_ACCOUNT_MAPPER.toLedgerAccountBO(LEDGER_ACCOUNT), SYSTEM);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getUserDetails()).isEqualTo(USER_NAME);
        assertThat(result.getName()).isEqualTo(LEDGER_ACCOUNT.getName());
        assertThat(result.getShortDesc()).isEqualTo(LEDGER_ACCOUNT.getShortDesc());
        assertThat(result.getLongDesc()).isEqualTo(LEDGER_ACCOUNT.getLongDesc());

        assertThat(result.getCoa()).isNotNull();
        assertThat(result.getLedger()).isNotNull();
    }

}
