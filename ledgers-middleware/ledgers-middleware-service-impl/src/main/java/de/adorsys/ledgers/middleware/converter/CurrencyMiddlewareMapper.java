package de.adorsys.ledgers.middleware.converter;

import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CurrencyMiddlewareMapper {
    public Currency toCurrency(String currency) {
        return currency == null
                       ? null
                       : Currency.getInstance(currency);
    }

    public String currencyToString(Currency currency) {
        return currency == null
                       ? null
                       : currency.getCurrencyCode();
    }
}
