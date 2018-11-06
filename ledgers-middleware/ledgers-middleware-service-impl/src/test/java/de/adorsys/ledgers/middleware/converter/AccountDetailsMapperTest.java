package de.adorsys.ledgers.middleware.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import pro.javatar.commons.reader.YamlReader;

public class AccountDetailsMapperTest {
    private AccountDetailsMapper mapper;

    @Before
    public void setUp() {
        mapper = Mappers.getMapper(AccountDetailsMapper.class);
    }

    @Test
    public void toAccountDetailsTO() throws IOException {
        AccountDetailsTO expected = getAccount(AccountDetailsTO.class);
        AccountDetailsTO details = mapper.toAccountDetailsTO(getAccount(DepositAccountBO.class), getBalances(BalanceBO.class));

        assertThat(details).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    public void toDepositAccountBO() throws IOException {
        DepositAccountBO details = mapper.toDepositAccountBO(getAccount(AccountDetailsTO.class));
        assertThat(details).isEqualToComparingFieldByFieldRecursively(getAccount(DepositAccountBO.class));
    }

    @Test
    public void toAccountBalancesTO() throws IOException {
        List<AccountBalanceTO> expected = getBalances(AccountBalanceTO.class);
        List<AccountBalanceTO> balances = mapper.toAccountBalancesTO(getBalances(BalanceBO.class));

        assertThat(balances).isNotEmpty();
        assertThat(balances.size()).isEqualTo(2);
        assertThat(balances.get(0)).isEqualToComparingFieldByFieldRecursively(expected.get(0));
        assertThat(balances.get(1)).isEqualToComparingFieldByFieldRecursively(expected.get(1));
    }


    private static <T> T getAccount(Class<T> aClass) throws IOException {
        return aClass.equals(AccountDetailsTO.class)
                       ? YamlReader.getInstance().getObjectFromResource(AccountDetailsMapper.class, "AccountDetailsTO.yml", aClass)
                       : YamlReader.getInstance().getObjectFromResource(AccountDetailsMapper.class, "AccountDetails.yml", aClass);
    }

    private static <T> List<T> getBalances(Class<T> tClass) throws IOException {
        return Arrays.asList(
                YamlReader.getInstance().getObjectFromResource(AccountDetailsMapper.class, "Balance1.yml", tClass),
                YamlReader.getInstance().getObjectFromResource(AccountDetailsMapper.class, "Balance2.yml", tClass)
        );
    }
}