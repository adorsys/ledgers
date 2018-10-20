package de.adorsys.ledgers.deposit.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.service.DepositAccountConfigService;
import de.adorsys.ledgers.postings.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import pro.javatar.commons.reader.YamlReader;

@RunWith(MockitoJUnitRunner.class)
public class ITDepositAccountServiceImplTest{
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

    @Test
    public void test_create_customer_bank_account_ok() throws LedgerAccountNotFoundException, LedgerNotFoundException, PaymentProcessingException {
        when(depositAccountConfigService.getDepositParentAccount()).thenReturn(LEDGER_ACCOUNT);
        when(ledgerService.newLedgerAccount(any())).thenReturn(LEDGER_ACCOUNT);
        when(depositAccountRepository.save(any())).thenReturn(getDepositAccount(DepositAccount.class));

        //Given
        DepositAccountBO da = getDepositAccount(DepositAccountBO.class);
        //When
        DepositAccountBO createdDepositAccount = depositAccountService.createDepositAccount(da);
        //Then
        assertThat(createdDepositAccount).isNotNull();
    }
    
    private <T> T getDepositAccount(Class<T> t) {
    	return YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/deposit/service/impl/ITDepositAccountServiceImplTest.yml", t);
    }
}