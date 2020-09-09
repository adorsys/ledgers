package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.BalanceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountDetailsMapperTest {
    private AccountDetailsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(AccountDetailsMapper.class);
    }

    @Test
    void toAccountDetailsTO() throws IOException {
        // Given
        AccountDetailsTO expected = getAccount(AccountDetailsTO.class);

        // When
        AccountDetailsTO details = mapper.toAccountDetailsTO(getAccount(DepositAccountBO.class), getBalances(BalanceBO.class));

        // Then
        assertThat(details).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void toDepositAccountBO() throws IOException {
        // When
        DepositAccountBO details = mapper.toDepositAccountBO(getAccount(AccountDetailsTO.class));

        // Then
        assertThat(details).isEqualToComparingFieldByFieldRecursively(getAccount(DepositAccountBO.class));
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