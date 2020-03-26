package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.util.exception.PostingModuleException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbstractServiceImplTest {
    private static final String ACCOUNT_ID = "ledger account id";
    private static final String ACCOUNT_NAME = "ledger account name";
    private static final String LEDGER_ID = "ledger id";
    private static final String LEDGER_NAME = "ledger name";

    @InjectMocks
    AbstractServiceImpl service;
    @Mock
    private LedgerAccountRepository ledgerAccountRepository;
    @Mock
    private ChartOfAccountRepository chartOfAccountRepo;
    @Mock
    private LedgerRepository ledgerRepository;

    @Test
    public void loadCoa() {
        when(chartOfAccountRepo.findById(anyString())).thenReturn(Optional.of(getCoa()));
        ChartOfAccount result = service.loadCoa(getCoaBO("id", "name"));
        assertThat(result).isEqualTo(new ChartOfAccount());
    }

    @Test
    public void loadCoa_id_not_present() {
        when(chartOfAccountRepo.findOptionalByName(anyString())).thenReturn(Optional.of(getCoa()));
        ChartOfAccount result = service.loadCoa(getCoaBO(null, "name"));
        assertThat(result).isEqualTo(new ChartOfAccount());
    }

    @Test(expected = PostingModuleException.class)
    public void loadCoa_no_identifier_present() {
        service.loadCoa(getCoaBO(null, null));
    }

    @Test(expected = PostingModuleException.class)
    public void loadCoa_by_id_not_found() {
        when(chartOfAccountRepo.findById(anyString())).thenReturn(Optional.empty());
        ChartOfAccount result = service.loadCoa(getCoaBO("id", "name"));
    }

    @Test(expected = PostingModuleException.class)
    public void loadCoa_by_name_not_found() {
        when(chartOfAccountRepo.findOptionalByName(anyString())).thenReturn(Optional.empty());
        ChartOfAccount result = service.loadCoa(getCoaBO(null, "name"));
    }

    @Test(expected = PostingModuleException.class)
    public void loadCoa_null_body() {
        service.loadCoa(null);
    }

    private ChartOfAccount getCoa() {
        return new ChartOfAccount();
    }

    private ChartOfAccountBO getCoaBO(String id, String name) {
        return new ChartOfAccountBO(name, id, LocalDateTime.now(), "details", "short descr", "long desc");
    }

    @Test
    public void loadLedgerAccountBO() {
        when(ledgerAccountRepository.findById(anyString())).thenReturn(Optional.of(new LedgerAccount()));
        LedgerBO ledger = getLedger(LEDGER_ID, LEDGER_NAME);
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(ACCOUNT_ID, ACCOUNT_NAME, ledger));
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    @Test
    public void loadLedgerAccountBO_by_name_n_ledger_id() {
        when(ledgerRepository.findById(anyString())).thenReturn(Optional.of(new Ledger()));
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.of(new LedgerAccount()));
        LedgerBO ledger = getLedger(LEDGER_ID, LEDGER_NAME);
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(null, ACCOUNT_NAME, ledger));
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    @Test
    public void loadLedgerAccountBO_by_name_n_ledger_name() {
        when(ledgerRepository.findOptionalByName(anyString())).thenReturn(Optional.of(new Ledger()));
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.of(new LedgerAccount()));
        LedgerBO ledger = getLedger(null, LEDGER_NAME);
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(null, ACCOUNT_NAME, ledger));
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedgerAccountBO_by_name_n_ledger_insufficient_ledger_info() {
        LedgerBO ledger = getLedger(null, null);
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(null, ACCOUNT_NAME, ledger));
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedgerAccountBO_by_id_nf() {
        when(ledgerAccountRepository.findById(anyString())).thenReturn(Optional.empty());
        LedgerBO ledger = getLedger(null, null);
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(ACCOUNT_ID, ACCOUNT_NAME, ledger));
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedgerAccountBO_by_name_nf() {
        when(ledgerRepository.findById(anyString())).thenReturn(Optional.of(new Ledger()));
        when(ledgerAccountRepository.findOptionalByLedgerAndName(any(), anyString())).thenReturn(Optional.empty());
        LedgerBO ledger = getLedger(LEDGER_ID, null);
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(null, ACCOUNT_NAME, ledger));
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedgerAccountBO_null() {
        LedgerAccount result = service.loadLedgerAccountBO(null);
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedgerAccountBO_null_info() {
        LedgerBO ledger = getLedger(LEDGER_ID, null);
        LedgerAccount result = service.loadLedgerAccountBO(getLedgerAccountBO(null, null, ledger));
        assertThat(result).isEqualTo(new LedgerAccount());
    }

    private LedgerAccountBO getLedgerAccountBO(String id, String name, LedgerBO ledger) {
        LedgerAccountBO account = new LedgerAccountBO();
        account.setId(id);
        account.setName(name);
        account.setLedger(ledger);
        return account;
    }

    private LedgerBO getLedger(String id, String name) {
        LedgerBO ledger = new LedgerBO();
        ledger.setId(id);
        ledger.setName(name);
        return ledger;
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedger() {
        service.loadLedger((LedgerBO) null);
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedger_id_nf() {
        when(ledgerRepository.findById(anyString())).thenReturn(Optional.empty());
        service.loadLedger(getLedger(LEDGER_ID, LEDGER_NAME));
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedger_name_nf() {
        when(ledgerRepository.findOptionalByName(anyString())).thenReturn(Optional.empty());
        service.loadLedger(getLedger(null, LEDGER_NAME));
    }

    @Test(expected = PostingModuleException.class)
    public void loadLedger_null() {
        service.loadLedger((Ledger) null);
    }
}