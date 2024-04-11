/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.util.exception.DepositErrorCode;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyExchangeRatesServiceImpl implements CurrencyExchangeRatesService {
    private static final Currency DEFAULT_ASPSP_CURRENCY = Currency.getInstance("EUR");
    private static final String CURRENCY_FIELD_NAME = "currency";
    private static final String RATE_FIELD_NAME = "rate";
    private static final String DEFAULT_RATE = "1";
    private static Map<Currency, String> rates = new HashMap<>();
    private static final String RATE_CONTRACT = "International Currency Exchange Market";

    private final ExchangeRateClient client;
    private final ObjectMapper objectMapper;

    @Override
    @Scheduled(cron = "0 0 8 * * MON-FRI")
    public void updateRates() throws IOException {
        try {
            String body = client.getRatesToEur().getBody();
            JSONObject json = XML.toJSONObject(body);
            JsonNode tree = objectMapper.readTree(json.toString()).findValue("Cube")
                                    .elements().next()
                                    .elements().next();
            tree.elements()
                    .forEachRemaining(r -> mapRate(r.get(RATE_FIELD_NAME), r.get(CURRENCY_FIELD_NAME), rates::put));
            log.info("ExchangeRates updated: {}", LocalDateTime.now());
        } catch (IOException | FeignException e) {
            log.error("Could not update ExchangeRates: {} resetting to default!", LocalDateTime.now());
            loadDefaultRates();
        }
    }

    @SneakyThrows(value = IOException.class)
    private void mapRate(JsonNode rate, JsonNode currency, BiConsumer<Currency, String> consumer) {
        Currency currencyValue = objectMapper.readValue(currency.toString(), Currency.class);
        rates.put(currencyValue, rate.asText());
        consumer.accept(currencyValue, rate.asText());
    }

    private static void loadDefaultRates() throws IOException {
        Resource resource = new DefaultResourceLoader().getResource("rates.yml");
        rates = new ObjectMapper(new YAMLFactory()).readValue(resource.getInputStream(), new TypeReference<Map<Currency, String>>() {
        });
        log.info("ExchangeRates updated to defaults: {}", LocalDateTime.now());
    }

    @Override
    public List<ExchangeRateBO> getExchangeRates(Currency debtor, Currency amount, Currency creditor) {
        if (debtor == amount && amount == creditor) {
            return Collections.emptyList();
        }
        List<ExchangeRateBO> ratesToReturn = new ArrayList<>();
        updateRatesList(amount, debtor, rates, ratesToReturn);
        updateRatesList(amount, creditor, rates, ratesToReturn);
        return ratesToReturn;
    }

    @Override
    public BigDecimal applyRate(BigDecimal amount, ExchangeRateBO rate) {
        return Optional.ofNullable(rate)
                       .map(r -> amount.divide(parseBD(r.getRateFrom()), 4, RoundingMode.HALF_EVEN).multiply(parseBD(r.getRateTo())))
                       .orElse(amount);
    }

    @Override
    public BigDecimal applyRate(Currency curFrom, Currency curTo, BigDecimal value) {
        if (curFrom == curTo) {
            return value;
        }
        ExchangeRateBO rate = resolveExchangeRate(curFrom, curTo, rates);
        return applyRate(value, rate);
    }

    private BigDecimal parseBD(String value) {
        return NumberUtils.createBigDecimal(value);
    }

    private void updateRatesList(Currency curFrom, Currency curTo, Map<Currency, String> rates, List<ExchangeRateBO> ratesToReturn) {
        if (curFrom != curTo) {
            ExchangeRateBO rate = resolveExchangeRate(curFrom, curTo, rates);
            if (!rate.getRateFrom().equals(rate.getRateTo())) {
                ratesToReturn.add(rate);
            }
        }
    }

    private ExchangeRateBO resolveExchangeRate(Currency curFrom, Currency curTo, Map<Currency, String> rates) {
        if (curFrom != curTo) {
            String rateFrom = resolveRate(curFrom, rates);
            String rateTo = resolveRate(curTo, rates);
            if (!rateFrom.equals(rateTo)) {
                return new ExchangeRateBO(curFrom, rateFrom, curTo, rateTo, LocalDate.now(), RATE_CONTRACT);
            }
        }
        return new ExchangeRateBO(curFrom, DEFAULT_RATE, curTo, DEFAULT_RATE, LocalDate.now(), RATE_CONTRACT);
    }

    private String resolveRate(Currency currency, Map<Currency, String> rates) {
        return currency.equals(DEFAULT_ASPSP_CURRENCY)
                       ? DEFAULT_RATE
                       : getRate(currency, rates);
    }

    private String getRate(Currency currency, Map<Currency, String> rates) {
        return Optional.ofNullable(rates.get(currency))
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(DepositErrorCode.CURRENCY_NOT_SUPPORTED)
                                                  .devMsg(String.format("Exchange rate for currency: %s not found", currency))
                                                  .build());
    }
}
