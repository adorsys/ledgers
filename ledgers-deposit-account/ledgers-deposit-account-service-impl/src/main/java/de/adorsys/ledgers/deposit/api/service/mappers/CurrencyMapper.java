package de.adorsys.ledgers.deposit.api.service.mappers;

import java.util.Currency;

public class CurrencyMapper {
    public Currency toCurrency(String currency) {
        return Currency.getInstance(currency);
    }

    public String currencyToString(Currency currency) {
        return currency.getCurrencyCode();
    }
}
