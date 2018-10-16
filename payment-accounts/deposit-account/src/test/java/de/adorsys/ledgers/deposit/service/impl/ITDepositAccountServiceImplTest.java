package de.adorsys.ledgers.deposit.service.impl;

import de.adorsys.ledgers.deposit.domain.AccountStatus;
import de.adorsys.ledgers.deposit.domain.AccountType;
import de.adorsys.ledgers.deposit.domain.AccountUsage;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.service.DepositAccountService;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.LedgerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ITDepositAccountServiceImplTest extends DepositAccountServiceImpl {
    private static final String LEDGER_ACCOUNT_ID = "1234567890";
    private static final LedgerAccount LEDGER_ACCOUNT = LedgerAccount.builder().id(LEDGER_ACCOUNT_ID).build();

    @InjectMocks
    private DepositAccountService depositAccountService = new DepositAccountServiceImpl();

    @Mock
    private DepositAccountRepository depositAccountRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;

    @Test
    public void test_create_customer_bank_account_ok() throws NotFoundException {
        when(depositAccountConfigService.getDepositParentAccount()).thenReturn(LEDGER_ACCOUNT);
        when(ledgerService.newLedgerAccount(any())).thenReturn(LEDGER_ACCOUNT);
        when(depositAccountRepository.save(any())).thenReturn(getDepositAccount());

        //Given
        DepositAccount da = getDepositAccount();
        //When
        DepositAccount createdDepositAccount = depositAccountService.createDepositAccount(da);
        //Then
        assertThat(createdDepositAccount).isNotNull();
    }

    private DepositAccount getDepositAccount() {
        return DepositAccount.builder()
                       .iban("DE91100000000123456789")
                       .currency(Currency.getInstance("EUR"))
                       .name("Account mykola")
                       .product("Deposit Account")
                       .accountType(AccountType.CASH)
                       .accountStatus(AccountStatus.ENABLED)
                       .usageType(AccountUsage.PRIV)
                       .details("Mykola's Account").build();
    }
}