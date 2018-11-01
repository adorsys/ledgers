package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.deposit.api.domain.AccountStatusBO;
import de.adorsys.ledgers.deposit.api.domain.AccountTypeBO;
import de.adorsys.ledgers.deposit.api.domain.AccountUsageBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountStatusTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountTypeTO;
import de.adorsys.ledgers.middleware.service.domain.account.UsageTypeTO;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountDetailsMapperTest {
    private AccountDetailsMapper mapper;

    @Before
    public void setUp() {
        mapper = Mappers.getMapper(AccountDetailsMapper.class);
    }

    @Test
    public void toAccountDetailsTO() throws IOException {
        AccountDetailsTO details = mapper.toAccountDetailsTO(getAccount(DepositAccountBO.class));

        assertThat(details.getId()).isEqualTo("id");
        assertThat(details.getIban()).isEqualTo("iban");
        assertThat(details.getBban()).isEqualTo("bban");
        assertThat(details.getPan()).isEqualTo("pan");
        assertThat(details.getMaskedPan()).isEqualTo("maskedPan");
        assertThat(details.getMsisdn()).isEqualTo("msisdn");
        assertThat(details.getCurrency()).isEqualTo(Currency.getInstance("EUR"));
        assertThat(details.getName()).isEqualTo("name");
        assertThat(details.getProduct()).isEqualTo("product");
        assertThat(details.getAccountType()).isEqualTo(AccountTypeTO.CASH);
        assertThat(details.getAccountStatus()).isEqualTo(AccountStatusTO.ENABLED);
        assertThat(details.getBic()).isEqualTo("bic");
        assertThat(details.getLinkedAccounts()).isEqualTo("account");
        assertThat(details.getUsageType()).isEqualTo(UsageTypeTO.PRIV);
        assertThat(details.getDetails()).isEqualTo("details");
        assertThat(details.getBalances()).isEqualTo(null);//TODO fix when balances will be added     by @dmiex
    }

    @Test
    public void toDepositAccountBO() throws IOException {
        DepositAccountBO details = mapper.toDepositAccountBO(getAccount(AccountDetailsTO.class));

        assertThat(details.getId()).isEqualTo("id");
        assertThat(details.getIban()).isEqualTo("iban");
        assertThat(details.getBban()).isEqualTo("bban");
        assertThat(details.getPan()).isEqualTo("pan");
        assertThat(details.getMaskedPan()).isEqualTo("maskedPan");
        assertThat(details.getMsisdn()).isEqualTo("msisdn");
        assertThat(details.getCurrency()).isEqualTo(Currency.getInstance("EUR"));
        assertThat(details.getName()).isEqualTo("name");
        assertThat(details.getProduct()).isEqualTo("product");
        assertThat(details.getAccountType()).isEqualTo(AccountTypeBO.CASH);
        assertThat(details.getAccountStatus()).isEqualTo(AccountStatusBO.ENABLED);
        assertThat(details.getBic()).isEqualTo("bic");
        assertThat(details.getLinkedAccounts()).isEqualTo("account");
        assertThat(details.getUsageType()).isEqualTo(AccountUsageBO.PRIV);
        assertThat(details.getDetails()).isEqualTo("details");
    }

    private static <T> T getAccount(Class<T> aClass) throws IOException {
        return YamlReader.getInstance().getObjectFromResource(AccountDetailsMapper.class, "AccountDetails.yml", aClass);
    }
}