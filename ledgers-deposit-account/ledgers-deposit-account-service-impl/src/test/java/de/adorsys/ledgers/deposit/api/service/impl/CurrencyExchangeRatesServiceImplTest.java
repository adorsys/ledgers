package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.FieldSetter.setField;

@ExtendWith(MockitoExtension.class)
class CurrencyExchangeRatesServiceImplTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency CHF = Currency.getInstance("CHF");


    private static final Map<Currency, String> INTERNAL_TEST_RATES;

    static {
        INTERNAL_TEST_RATES = new HashMap<>();
        INTERNAL_TEST_RATES.put(USD, "1.1115");
        INTERNAL_TEST_RATES.put(GBP, "0.84868");
        INTERNAL_TEST_RATES.put(CHF, "1.0792");
        INTERNAL_TEST_RATES.put(EUR, "1");
    }

    @InjectMocks
    private CurrencyExchangeRatesServiceImpl currencyExchangeRatesService;

    @Mock
    private ExchangeRateClient client;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Logger log;

    @Mock
    Map<Currency, String> rates;

    private JsonNode getNodes() {
        List<RateCube> collect = INTERNAL_TEST_RATES.entrySet().stream().map(e -> new RateCube(e.getKey(), e.getValue())).collect(Collectors.toList());
        return new ObjectMapper().valueToTree(new CubeEnvelope(Collections.singletonList(new Cube(collect))));
    }

    @Test
    void updateRates() throws IOException {
        when(client.getRatesToEur()).thenReturn(ResponseEntity.ok("<rate></rate>"));
        when(objectMapper.readTree(anyString())).thenReturn(getNodes());
        when(objectMapper.readValue(anyString(), any(Class.class))).thenAnswer(a -> new ObjectMapper().readValue((String) a.getArgument(0), Currency.class));
        currencyExchangeRatesService.updateRates();
        assertDoesNotThrow(DepositModuleException::builder);
    }

    @Test
    void updateRates_load_default() throws IOException {
        when(client.getRatesToEur()).thenThrow(FeignException.class);
        currencyExchangeRatesService.updateRates();
        assertDoesNotThrow(DepositModuleException::builder);
    }

    @Test
    void getExchangeRates_all_same() {
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
        //Given
        Currency uah = Currency.getInstance("UAH");
        // Then
        assertThrows(DepositModuleException.class, () -> {
            List<ExchangeRateBO> result = currencyExchangeRatesService.getExchangeRates(EUR, EUR, uah);
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
    void applyRate_2() throws NoSuchFieldException {
        setField(currencyExchangeRatesService, currencyExchangeRatesService.getClass().getDeclaredField("rates"), INTERNAL_TEST_RATES);
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

    private ExchangeRateBO getRate(Currency one, Currency two) {
        return new ExchangeRateBO(one, CurrencyExchangeRatesServiceImplTest.INTERNAL_TEST_RATES.get(one), two, CurrencyExchangeRatesServiceImplTest.INTERNAL_TEST_RATES.get(two), LocalDate.now(), "International Currency Exchange Market");
    }

    @Data
    @AllArgsConstructor
    private static class CubeEnvelope {
        @JsonProperty("Cube")
        List<Cube> cube;
    }

    @Data
    @AllArgsConstructor
    private static class Cube {
        @JsonProperty("Cube")
        List<RateCube> cube;
    }

    @Data
    private static class RateCube {
        private final Currency currency;
        private final String rate;
    }
}
