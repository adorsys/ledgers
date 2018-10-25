package de.adorsys.ledgers.deposit.api.service.mappers;

import org.junit.Test;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;


public class CurrencyMapperTest {
private CurrencyMapper currencyMapper = new CurrencyMapper();

    @Test
    public void toCurrency() {
        Currency currency = currencyMapper.toCurrency("EUR");
        assertThat(currency).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    public void currencyToString() {
        String currency = currencyMapper.currencyToString(Currency.getInstance("EUR"));
        assertThat(currency).isEqualTo("EUR");
    }
}