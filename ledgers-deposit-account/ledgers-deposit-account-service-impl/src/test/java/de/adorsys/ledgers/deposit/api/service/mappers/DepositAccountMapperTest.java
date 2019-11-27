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

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class DepositAccountMapperTest {

    @InjectMocks
    private DepositAccountMapperImpl depositAccountMapper;

    @Test
    public void toDepositAccountBO() {
        DepositAccountBO account = depositAccountMapper.toDepositAccountBO(getDepositAccount());

        //Then
        assertThat(account).isEqualToComparingFieldByFieldRecursively(getDepositAccountBO());
    }

    @Test
    public void toDepositAccount() {
        DepositAccount account = depositAccountMapper.toDepositAccount(getDepositAccountBO());

        //Then
        assertThat(account).isEqualToComparingFieldByFieldRecursively(getDepositAccount());
    }

    private DepositAccount getDepositAccount() {
        return new DepositAccount("id", "iban", "msisdn", "EUR",
                "name", "product", null, AccountType.CASH, AccountStatus.ENABLED, "bic",
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
