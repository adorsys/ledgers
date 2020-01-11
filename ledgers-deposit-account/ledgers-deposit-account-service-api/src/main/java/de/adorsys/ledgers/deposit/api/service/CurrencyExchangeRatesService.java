package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.ExchangeRateBO;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public interface CurrencyExchangeRatesService {
    Map<Currency, String> getAllRates();

    List<ExchangeRateBO> getExchangeRates(Currency debtor, Currency amount, Currency creditor);

    /*
      example:
      Debtor cur:   GBP
      Payment cur:  USD
      Creditor cur: JPY
      <p>
      Rates : 1) from: USD - EUR; to: EUR - GBP
      2) from: USD - EUR; to: EUR - JPY
      <p>
      lines amount = amount / rateFrom * rateTo (USD - EUR - GBP) (USD - EUR - JPY)
      */

    /**
     * @param amount big decimal amount
     * @param rate   rate to apply to amount
     * @return big decimal representation of value with rate applied
     */
    BigDecimal applyRate(BigDecimal amount, ExchangeRateBO rate);

    /**
     * From amount to debtor or from amount to creditor
     *
     * @param from  amount currency
     * @param to    debtor/creditor currency
     * @param value big decimal value to apply rate to
     * @return big decimal representation of value with rate applied
     */
    BigDecimal applyRate(Currency from, Currency to, BigDecimal value);
}
