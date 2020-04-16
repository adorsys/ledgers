package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeRatesServiceImplTest {
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

    private JsonNode getNodes() {
        List<RateCube> collect = rates.entrySet().stream().map(e -> new RateCube(e.getKey(), e.getValue())).collect(Collectors.toList());
        return new ObjectMapper().valueToTree(collect);
    }

    @Test
    void getExchangeRates_all_same() {
        // Given
     /*   when(client.getRatesToEur()).thenReturn(ResponseEntity.ok(new CubeType()));
        when(objectMapper.valueToTree(any())).thenReturn(getNodes());
        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, USD, GBP);
        List<ExchangeRateBO> expected = getExpected(USD, EUR, USD, GBP);
        assertThat(result).isEqualTo(expected);*/

        // When
        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, EUR, EUR);

        // Then
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void getExchangeRates_creditor_currency_differs() {
        // When
        List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, EUR, USD);

        // Then
        assertEquals(Collections.singletonList(getRate(EUR, USD)), result);
    }

    @Test
    void getExchangeRates_currency_not_supported() {
        // Then
        assertThrows(DepositModuleException.class, () -> {
            List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, EUR, Currency.getInstance("UAH"));
            assertEquals(Collections.singletonList(getRate(EUR, USD)), result);
        });
    }

    @Test
    void applyRate() {
        // When
        BigDecimal result = currencyExchangeRatesService.applyRate(BigDecimal.TEN, getRate(EUR, USD));

        // Then
        assertThat(result, Matchers.comparesEqualTo(BigDecimal.valueOf(11.11500000)));
    }

    @Test
    void applyRate_2() {
        // When
        BigDecimal result = currencyExchangeRatesService.applyRate(EUR, USD, BigDecimal.TEN);

        // Then
        assertThat(result, Matchers.comparesEqualTo(BigDecimal.valueOf(11.11500000)));
    }

    @Test
    void applyRate_2_same_currencies() {
        // When
        BigDecimal result = currencyExchangeRatesService.applyRate(USD, USD, BigDecimal.TEN);

        // Then
        assertThat(result, Matchers.comparesEqualTo(BigDecimal.TEN));
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
