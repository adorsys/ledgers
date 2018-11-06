package de.adorsys.ledgers.deposit.api.domain;

import java.time.LocalDate;
import java.util.Currency;

public class ExchangeRateBO {
    Currency currencyFrom;
    String rateFrom;
    Currency currencyTo;
    String rateTo;
    LocalDate rateDate;
    String rateContract;

    public ExchangeRateBO(Currency currencyFrom, String rateFrom, Currency currencyTo, String rateTo, LocalDate rateDate, String rateContract) {
        this.currencyFrom = currencyFrom;
        this.rateFrom = rateFrom;
        this.currencyTo = currencyTo;
        this.rateTo = rateTo;
        this.rateDate = rateDate;
        this.rateContract = rateContract;
    }

    public ExchangeRateBO() {
    }

    //Getters-setters
    public Currency getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(Currency currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public String getRateFrom() {
        return rateFrom;
    }

    public void setRateFrom(String rateFrom) {
        this.rateFrom = rateFrom;
    }

    public Currency getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(Currency currencyTo) {
        this.currencyTo = currencyTo;
    }

    public String getRateTo() {
        return rateTo;
    }

    public void setRateTo(String rateTo) {
        this.rateTo = rateTo;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public void setRateDate(LocalDate rateDate) {
        this.rateDate = rateDate;
    }

    public String getRateContract() {
        return rateContract;
    }

    public void setRateContract(String rateContract) {
        this.rateContract = rateContract;
    }
}
