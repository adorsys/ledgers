package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.util.exception.DepositErrorCode;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeRatesServiceImpl implements CurrencyExchangeRatesService {
    private static final Currency DEFAULT_ASPSP_CURRENCY = Currency.getInstance("EUR");
    private static final String CURRENCY_FIELD_NAME = "currency";
    private static final String RATE_FIELD_NAME = "rate";
    private static final String DEFAULT_RATE = "1";
    private final ExchangeRateClient client;
    private final ObjectMapper objectMapper;

    /*@Override //TODO Restore functionality overriding xmlObjectMapper for Feign to enable reading JAXB annotations
    public Map<Currency, String> getAllRates() {
        CubeType body = client.getRatesToEur().getBody();
        List<JsonNode> parents = objectMapper.valueToTree(body).findParents(CURRENCY_FIELD_NAME);
        return parents.stream()
                       .filter(n -> StringUtils.isNotBlank(n.get(CURRENCY_FIELD_NAME).textValue()))
                       .collect(Collectors.toMap(this::getCurrency, this::getRateFromNode));
    }*/

    @Override
    public Map<Currency, String> getAllRates() {
        Map<Currency, String> rates = new HashMap<>();
        rates.put(Currency.getInstance("USD"), "1.1115");
        rates.put(Currency.getInstance("JPY"), "120.86");
        rates.put(Currency.getInstance("BGN"), "1.9558");
        rates.put(Currency.getInstance("CZK"), "25.265");
        rates.put(Currency.getInstance("DKK"), "7.4731");
        rates.put(Currency.getInstance("GBP"), "0.84868");
        rates.put(Currency.getInstance("HUF"), "331.08");
        rates.put(Currency.getInstance("PLN"), "4.2429");
        rates.put(Currency.getInstance("RON"), "4.7774");
        rates.put(Currency.getInstance("SEK"), "10.5108");
        rates.put(Currency.getInstance("CHF"), "1.0792");
        rates.put(Currency.getInstance("ISK"), "137.10");
        rates.put(Currency.getInstance("NOK"), "9.8508");
        rates.put(Currency.getInstance("HRK"), "7.4490");
        rates.put(Currency.getInstance("RUB"), "68.6389");
        rates.put(Currency.getInstance("TRY"), "6.6158");
        rates.put(Currency.getInstance("AUD"), "1.6195");
        rates.put(Currency.getInstance("BRL"), "4.5092");
        rates.put(Currency.getInstance("CAD"), "1.4470");
        rates.put(Currency.getInstance("CNY"), "7.7184");
        rates.put(Currency.getInstance("HKD"), "8.6424");
        rates.put(Currency.getInstance("IDR"), "15441.51");
        rates.put(Currency.getInstance("ILS"), "3.8541");
        rates.put(Currency.getInstance("INR"), "79.7090");
        rates.put(Currency.getInstance("KRW"), "1297.37");
        rates.put(Currency.getInstance("MXN"), "20.9079");
        rates.put(Currency.getInstance("MYR"), "4.5588");
        rates.put(Currency.getInstance("NZD"), "1.6739");
        rates.put(Currency.getInstance("PHP"), "56.420");
        rates.put(Currency.getInstance("SGD"), "1.5014");
        rates.put(Currency.getInstance("THB"), "33.740");
        rates.put(Currency.getInstance("ZAR"), "15.8166");
        return rates;
    }

    @Override
    public List<ExchangeRateBO> getExchangeRates(Currency debtor, Currency amount, Currency creditor) {
        if (debtor.equals(amount) && amount.equals(creditor)) {
            return Collections.emptyList();
        }
        Map<Currency, String> rates = getAllRates();
        List<ExchangeRateBO> ratesToReturn = new ArrayList<>();
        updateRatesList(amount, debtor, rates, ratesToReturn);
        updateRatesList(amount, creditor, rates, ratesToReturn);
        return ratesToReturn;
    }

    private void updateRatesList(Currency from, Currency to, Map<Currency, String> rates, List<ExchangeRateBO> ratesToReturn) {
        if (!from.equals(to)) {
            String rateFrom = resolveRate(from, rates);
            String rateTo = resolveRate(to, rates);
            if (!rateFrom.equals(rateTo)) {
                ratesToReturn.add(new ExchangeRateBO(from, rateFrom, to, rateTo, LocalDate.now(), "International Currency Exchange Market"));
            }
        }
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

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private Currency getCurrency(JsonNode node) {
        return Currency.getInstance(node.get(CURRENCY_FIELD_NAME).textValue());
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private String getRateFromNode(JsonNode node) {
        return node.get(RATE_FIELD_NAME).asText();
    }
}
