package de.adorsys.ledgers.deposit.api.service.mappers;

import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class CurrencyMapper {
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
