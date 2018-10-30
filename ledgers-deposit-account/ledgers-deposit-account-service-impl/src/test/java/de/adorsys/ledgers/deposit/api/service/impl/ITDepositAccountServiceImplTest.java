package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ITDepositAccountServiceImplTest {
    private static final String LEDGER_ACCOUNT_ID = "1234567890";
    private static final LedgerAccountBO LEDGER_ACCOUNT = newLedgerAccountBO(LEDGER_ACCOUNT_ID);

    private static LedgerAccountBO newLedgerAccountBO(String id) {
        LedgerAccountBO l = new LedgerAccountBO();
        l.setId(id);
        return l;
    }

    @InjectMocks
    private DepositAccountServiceImpl depositAccountService;

    @Mock
    private DepositAccountRepository depositAccountRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;
    @Mock
    private DepositAccountMapper depositAccountMapper;

    @Test
    public void test_create_customer_bank_account_ok() throws LedgerAccountNotFoundException, LedgerNotFoundException, PaymentProcessingException, IOException {
        when(depositAccountConfigService.getDepositParentAccount()).thenReturn(LEDGER_ACCOUNT);
        when(ledgerService.newLedgerAccount(any())).thenReturn(LEDGER_ACCOUNT);
        when(depositAccountRepository.save(any())).thenReturn(getDepositAccount(DepositAccount.class));
        when(depositAccountMapper.toDepositAccount(any())).thenReturn(getDepositAccount(DepositAccount.class));
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(getDepositAccount(DepositAccountBO.class));

        //Given
        DepositAccountBO da = getDepositAccount(DepositAccountBO.class);
        //When
        DepositAccountBO createdDepositAccount = depositAccountService.createDepositAccount(da);
        //Then
        assertThat(createdDepositAccount, is(CoreMatchers.notNullValue()));
    }

    private <T> T getDepositAccount(Class<T> t) throws IOException {
        return YamlReader.getInstance().getObjectFromResource(ITDepositAccountServiceImplTest.class, "ITDepositAccountServiceImplTest.yml", t);
    }
}