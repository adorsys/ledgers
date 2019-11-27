package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.client.ExchangeRateClient;
import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;
import de.adorsys.ledgers.deposit.api.domain.exchange.CubeType;
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.util.exception.DepositErrorCode;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeRatesServiceImpl implements CurrencyExchangeRatesService {
    private static final Currency DEFAULT_ASPSP_CURRENCY = Currency.getInstance("EUR");
    private static final String CURRENCY_FIELD_NAME = "currency";
    private static final String RATE_FIELD_NAME = "rate";
    private static final String DEFAULT_RATE = "1";
    private final ExchangeRateClient client;
    private final ObjectMapper objectMapper;

    @Override
    public Map<Currency, String> getAllRates() {
        CubeType body = client.getRatesToEur().getBody();
        List<JsonNode> parents = objectMapper.valueToTree(body).findParents(CURRENCY_FIELD_NAME);
        return parents.stream()
                       .filter(n -> StringUtils.isNotBlank(n.get(CURRENCY_FIELD_NAME).textValue()))
                       .collect(Collectors.toMap(this::getCurrency, this::getRateFromNode));
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

    private Currency getCurrency(JsonNode node) {
        return Currency.getInstance(node.get(CURRENCY_FIELD_NAME).textValue());
    }

    private String getRateFromNode(JsonNode node) {
        return node.get(RATE_FIELD_NAME).asText();
    }
}
