package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyExchangeRatesServiceImplTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency CHF = Currency.getInstance("CHF");

    private static final Map<Currency, String> rates;

    static {
        rates = new HashMap<>();
        rates.put(USD, "1.1115");
        rates.put(GBP, "0.84868");
        rates.put(CHF, "1.0792");
        rates.put(EUR, "1");
    }

    @InjectMocks
    private CurrencyExchangeRatesServiceImpl currencyExchangeRatesService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ExchangeRateClient client;

    private JsonNode getNodes() {
        List<RateCube> collect = rates.entrySet().stream().map(e -> new RateCube(e.getKey(), e.getValue())).collect(Collectors.toList());
        return new ObjectMapper().valueToTree(collect);
    }

    @Test
    public void getExchangeRates_all_same() {
     /*   when(client.getRatesToEur()).thenReturn(ResponseEntity.ok(new CubeType()));
        when(objectMapper.valueToTree(any())).thenReturn(getNodes());
        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, USD, GBP);
        List<ExchangeRateBO> expected = getExpected(USD, EUR, USD, GBP);
        assertThat(result).isEqualTo(expected);*/

        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, EUR, EUR);
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    public void getExchangeRates_creditor_currency_differs() {
        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, EUR, USD);
        assertThat(result).isEqualTo(Collections.singletonList(getRate(EUR, USD)));
    }

    @Test(expected = DepositModuleException.class)
    public void getExchangeRates_currency_not_supported() {
        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, EUR, Currency.getInstance("UAH"));
        assertThat(result).isEqualTo(Collections.singletonList(getRate(EUR, USD)));
    }

    @Test
    public void applyRate() {
        BigDecimal result = currencyExchangeRatesService.applyRate(BigDecimal.TEN, getRate(EUR, USD));
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(11.11500000));
    }

    @Test
    public void applyRate_2() {
        BigDecimal result = currencyExchangeRatesService.applyRate(EUR, USD, BigDecimal.TEN);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(11.11500000));
    }

    @Test
    public void applyRate_2_same_currencies() {
        BigDecimal result = currencyExchangeRatesService.applyRate(USD, USD, BigDecimal.TEN);
        assertThat(result).isEqualByComparingTo(BigDecimal.TEN);
    }

    private List<ExchangeRateBO> getExpected(Currency one, Currency two, Currency three, Currency four) {
        List<ExchangeRateBO> rates = new ArrayList<>();
        rates.add(getRate(one, two));
        if (three != null) {
            ExchangeRateBO rate = getRate(three, four);
            rates.add(rate);
        }
        return rates;
    }

    private ExchangeRateBO getRate(Currency one, Currency two) {
        return new ExchangeRateBO(one, CurrencyExchangeRatesServiceImplTest.rates.get(one), two, CurrencyExchangeRatesServiceImplTest.rates.get(two), LocalDate.now(), "International Currency Exchange Market");
    }

    @Data
    private static class RateCube {
        private final Currency currency;
        private final String rate;
    }
}
