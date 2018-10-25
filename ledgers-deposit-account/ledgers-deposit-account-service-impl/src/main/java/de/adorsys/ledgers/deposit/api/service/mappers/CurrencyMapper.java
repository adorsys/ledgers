package de.adorsys.ledgers.deposit.api.service.mappers;

import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CurrencyMapper {
    public Currency toCurrency(String currency) {
        return Currency.getInstance(currency);
    }

    public String currencyToString(Currency currency) {
        return currency.getCurrencyCode();
    }
}
