package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.AccountStatusBO;
import de.adorsys.ledgers.deposit.api.domain.AccountTypeBO;
import de.adorsys.ledgers.deposit.api.domain.AccountUsageBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.impl.DepositAccountServiceImpl;
import de.adorsys.ledgers.deposit.db.domain.AccountStatus;
import de.adorsys.ledgers.deposit.db.domain.AccountType;
import de.adorsys.ledgers.deposit.db.domain.AccountUsage;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountServiceImplTest {
    @Mock
    private DepositAccountRepository depositAccountRepository;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private PostingService postingService;
    @Mock
    private DepositAccountConfigService depositAccountConfigService;
    @Mock
    private DepositAccountMapper depositAccountMapper = Mappers.getMapper(DepositAccountMapper.class);

    @InjectMocks
    private DepositAccountServiceImpl depositAccountService;

    @Test
    public void createDepositAccount() throws PaymentProcessingException, LedgerAccountNotFoundException, LedgerNotFoundException {
        when(depositAccountConfigService.getDepositParentAccount()).thenReturn(getLedgerAccountBO());
        when(ledgerService.newLedgerAccount(any())).thenReturn(getLedgerAccountBO());
        when(depositAccountRepository.save(any())).thenReturn(getDepositAccount());
        when(depositAccountMapper.toDepositAccount(any())).thenReturn(getDepositAccount());
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(getDepositAccountBO());

        DepositAccountBO depositAccount = depositAccountService.createDepositAccount(getDepositAccountBO());

        assertThat(depositAccount).isNotNull();
        assertThat(depositAccount.getId()).isNotBlank();
    }

    @Test
    public void getDepositAccountById() throws DepositAccountNotFoundException {
        when(depositAccountRepository.findById(any())).thenReturn(Optional.of(getDepositAccount()));
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(getDepositAccountBO());
        //When
        DepositAccountBO account = depositAccountService.getDepositAccountById("id");
        //Then
        assertThat(account).isNotNull();
    }

    @Test(expected = DepositAccountNotFoundException.class)
    public void getDepositAccountById_wrong_id() throws DepositAccountNotFoundException {
        when(depositAccountRepository.findById("wrong_id")).thenReturn(Optional.empty());
        //When
        DepositAccountBO account = depositAccountService.getDepositAccountById("wrong_id");
    }

    @Test
    public void getDepositAccountByIBAN() throws DepositAccountNotFoundException {
        when(depositAccountRepository.findByIban(any())).thenReturn(Optional.of(getDepositAccount()));
        when(depositAccountMapper.toDepositAccountBO(any())).thenReturn(getDepositAccountBO());
        //When
        DepositAccountBO account = depositAccountService.getDepositAccountByIBAN("iban");
        //Then
        assertThat(account).isNotNull();
    }

    @Test
    public void executeSinglePaymentWithoutSca() {
        //TODO to be implemented + failure cases
    }

    @Test
    public void executeSinglePaymentsWithoutSca() {
        //TODO to be implemented + failure cases
    }

    @Test
    public void executeBulkPaymentWithoutSca() {
        //TODO to be implemented + failure cases
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", Currency.getInstance("EUR"),
                "name", "product", AccountType.CASH, AccountStatus.ENABLED, "bic", null,
                AccountUsage.PRIV, "details");
    }

    private DepositAccountBO getDepositAccountBO() {
        DepositAccountBO bo = new DepositAccountBO();
        bo.setId("id");
        bo.setIban("iban");
        bo.setMsisdn("msisdn");
        bo.setCurrency(Currency.getInstance("EUR"));
        bo.setName("name");
        bo.setProduct("product");
        bo.setAccountType(AccountTypeBO.CASH);
        bo.setAccountStatus(AccountStatusBO.ENABLED);
        bo.setBic("bic");
        bo.setUsageType(AccountUsageBO.PRIV);
        bo.setDetails("details");
        return bo;
    }

    private LedgerAccountBO getLedgerAccountBO() {
        LedgerAccountBO bo = new LedgerAccountBO();
        bo.setId("id");
        bo.setName("name");

        return bo;
    }
}