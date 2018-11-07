package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.deposit.api.domain.AccountStatusBO;
import de.adorsys.ledgers.deposit.api.domain.AccountTypeBO;
import de.adorsys.ledgers.deposit.api.domain.AccountUsageBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.db.domain.AccountStatus;
import de.adorsys.ledgers.deposit.db.domain.AccountType;
import de.adorsys.ledgers.deposit.db.domain.AccountUsage;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class DepositAccountMapperTest {

    @InjectMocks
    private DepositAccountMapperImpl depositAccountMapper;
    @Mock
    private CurrencyMapper currencyMapper;

    @Test
    public void toDepositAccountBO() {
        when(currencyMapper.toCurrency(any())).thenReturn(Currency.getInstance("EUR"));
        DepositAccountBO account = depositAccountMapper.toDepositAccountBO(getDepositAccount());

        //Then
        assertThat(account.getId()).isEqualTo(getDepositAccountBO().getId());
        assertThat(account.getIban()).isEqualTo(getDepositAccountBO().getIban());
        assertThat(account.getMsisdn()).isEqualTo(getDepositAccountBO().getMsisdn());
        assertThat(account.getCurrency()).isEqualTo(getDepositAccountBO().getCurrency());
        assertThat(account.getName()).isEqualTo(getDepositAccountBO().getName());
        assertThat(account.getProduct()).isEqualTo(getDepositAccountBO().getProduct());
        assertThat(account.getAccountType()).isEqualTo(getDepositAccountBO().getAccountType());
        assertThat(account.getAccountStatus()).isEqualTo(getDepositAccountBO().getAccountStatus());
        assertThat(account.getBic()).isEqualTo(getDepositAccountBO().getBic());
        assertThat(account.getLinkedAccounts()).isEqualTo(getDepositAccountBO().getLinkedAccounts());
        assertThat(account.getUsageType()).isEqualTo(getDepositAccountBO().getUsageType());
        assertThat(account.getDetails()).isEqualTo(getDepositAccountBO().getDetails());
    }

    @Test
    public void toDepositAccount() {
        when(currencyMapper.currencyToString(any())).thenReturn("EUR");
        DepositAccount account = depositAccountMapper.toDepositAccount(getDepositAccountBO());

        //Then
        assertThat(account.getId()).isEqualTo(getDepositAccount().getId());
        assertThat(account.getIban()).isEqualTo(getDepositAccount().getIban());
        assertThat(account.getMsisdn()).isEqualTo(getDepositAccount().getMsisdn());
        assertThat(account.getCurrency()).isEqualTo(getDepositAccount().getCurrency());
        assertThat(account.getName()).isEqualTo(getDepositAccount().getName());
        assertThat(account.getProduct()).isEqualTo(getDepositAccount().getProduct());
        assertThat(account.getAccountType()).isEqualTo(getDepositAccount().getAccountType());
        assertThat(account.getAccountStatus()).isEqualTo(getDepositAccount().getAccountStatus());
        assertThat(account.getBic()).isEqualTo(getDepositAccount().getBic());
        assertThat(account.getLinkedAccounts()).isEqualTo(getDepositAccount().getLinkedAccounts());
        assertThat(account.getUsageType()).isEqualTo(getDepositAccount().getUsageType());
        assertThat(account.getDetails()).isEqualTo(getDepositAccount().getDetails());
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", Currency.getInstance("EUR"),
                "name", "product", AccountType.CASH, AccountStatus.ENABLED, "bic",
                "Some linked account", AccountUsage.PRIV, "details");
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
        bo.setLinkedAccounts("Some linked account");
        bo.setUsageType(AccountUsageBO.PRIV);
        bo.setDetails("details");
        return bo;
    }
}