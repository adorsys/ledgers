/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import de.adorsys.ledgers.middleware.impl.config.CurrencyConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyConfigurationProperties currencyConfigProp;

    @Override
    public Set<Currency> getSupportedCurrencies() {
        return currencyConfigProp.getCurrencies();
    }

    @Override
    public boolean isCurrencyValid(Currency currency) {
        return getSupportedCurrencies().contains(currency);
    }
}
