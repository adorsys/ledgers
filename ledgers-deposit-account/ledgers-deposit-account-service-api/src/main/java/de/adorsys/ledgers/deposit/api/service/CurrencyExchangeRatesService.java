package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;

import java.util.Currency;
import java.util.List;
import java.util.Map;

public interface CurrencyExchangeRatesService {
    Map<Currency, String> getAllRates();

    List<ExchangeRateBO> getExchangeRates(Currency debtor, Currency amount, Currency creditor);
}
