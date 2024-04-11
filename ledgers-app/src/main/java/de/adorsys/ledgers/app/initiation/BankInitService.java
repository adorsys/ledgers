/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankInitService {
    private final DepositAccountInitService depositAccountInitService;
    private final CurrencyExchangeRatesService exchangeRatesService;
    private final ApplicationContext context;
    private final IdpConfigurationService idpConfigurationService;
    private final TestDataUserService tDataUserService;
    private final TestDataAccountService tDataAccountService;
    private final RestTransactionService transactionService;

    public void init() {
        depositAccountInitService.initConfigData();
        idpConfigurationService.configureIDP();
        idpConfigurationService.createUpdateXs2aAdmin();
        try {
            exchangeRatesService.updateRates();
        } catch (IOException e) {
            log.error("ExchangeRate update failed for external service and default values on: {}, service is discontinued until Rate Service is fixed!", LocalDateTime.now());
            SpringApplication.exit(context, () -> 0);
        }
    }

    public void uploadTestData() {
        tDataUserService.createUsers();
        tDataAccountService.createAccounts();
        transactionService.performTransactions();
    }
}
