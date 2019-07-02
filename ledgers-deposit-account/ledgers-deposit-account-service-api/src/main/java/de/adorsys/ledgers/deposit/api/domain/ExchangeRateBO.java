package de.adorsys.ledgers.deposit.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Currency;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateBO {
    Currency currencyFrom;
    String rateFrom;
    Currency currencyTo;
    String rateTo;
    LocalDate rateDate;
    String rateContract;
}
