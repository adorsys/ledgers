package de.adorsys.ledgers.middleware.converter;

import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.postings.api.domain.BalanceBO;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class AccountBalancesMapperTest {

    private AccountBalancesMapper balancesMapper = Mappers.getMapper(AccountBalancesMapper.class);
    @Test
    public void toAccountBalancesTO() throws IOException {

        List<AccountBalanceTO> balances = balancesMapper.toAccountBalancesTO(getBalances(List<BalanceBO.class>));

    }

    private static<T> List<T> getBalances() throws IOException {
        return YamlReader.getInstance().getObjectFromResource(AccountBalancesMapper.class, "Balances.yml", );
    }
}