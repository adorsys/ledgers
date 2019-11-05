package de.adorsys.ledgers.middleware.api.service;

import java.util.Currency;
import java.util.Set;

public interface CurrencyService {

    /**
     * @return list of supported currencies
     */
    Set<Currency> getSupportedCurrencies();

    /**
     * Return boolean value if currency is supported
     *
     * @param currency currency used
     */
    boolean isCurrencyValid(Currency currency);
}
