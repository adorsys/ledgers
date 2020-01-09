package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import lombok.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyExchangeRatesServiceImplTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency CHF = Currency.getInstance("CHF");

    private static final Map<Currency, String> rates;

    static {
        rates = new HashMap<>();
        rates.put(USD, "1.2");
        rates.put(GBP, "0.8");
        rates.put(CHF, "0.5");
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
    public void getExchangeRates() {
     /*   when(client.getRatesToEur()).thenReturn(ResponseEntity.ok(new CubeType()));
        when(objectMapper.valueToTree(any())).thenReturn(getNodes());
        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, USD, GBP);
        List<ExchangeRateBO> expected = getExpected(USD, EUR, USD, GBP);
        assertThat(result).isEqualTo(expected);*/
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
    private class RateCube {
        private final Currency currency;
        private final String rate;
    }
}
