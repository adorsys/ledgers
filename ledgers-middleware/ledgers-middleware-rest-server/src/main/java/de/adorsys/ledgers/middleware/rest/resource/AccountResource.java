/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.REQUEST_VALIDATION_FAILURE;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(AccountRestAPI.BASE_PATH)
public class AccountResource implements AccountRestAPI {
    private final ScaInfoHolder scaInfoHolder;
    private final MiddlewareAccountManagementService middlewareAccountService;


    /**
     * Return the list of accounts linked with the current customer.
     *
     * @return : the list of accounts linked with the current customer.
     */
    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccounts() {
        return ResponseEntity.ok(middlewareAccountService.listDepositAccounts(scaInfoHolder.getUserId()));
    }

    @Override
    @PreAuthorize("hasRole('CUSTOMER') and tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<Void> createDepositAccount(AccountDetailsTO accountDetailsTO) {
        // create account. It does not exist.
        String iban = accountDetailsTO.getIban();
        // Splitt in prefix and suffix
        String accountNumberPrefix = StringUtils.substring(iban, 0, iban.length() - 2);
        String accountNumberSuffix = StringUtils.substringAfter(iban, accountNumberPrefix);

        middlewareAccountService.createDepositAccount(scaInfoHolder.getScaInfo(), accountNumberPrefix, accountNumberSuffix, accountDetailsTO);
        // TODO: return 201 and link to account.
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsById(String accountId) {
        return ResponseEntity.ok(middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true));
    }

    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<AccountBalanceTO>> getBalances(String accountId) {
        AccountDetailsTO accountDetails = middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true);
        return ResponseEntity.ok(accountDetails.getBalances());
    }

    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<TransactionTO>> getTransactionByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) {
        dateChecker(dateFrom, dateTo);
        List<TransactionTO> transactions = middlewareAccountService.getTransactionsByDates(accountId, validDate(dateFrom), validDate(dateTo));
        return ResponseEntity.ok(transactions);
    }

    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<TransactionTO> getTransactionById(String accountId, String transactionId) {
        return ResponseEntity.ok(middlewareAccountService.getTransactionById(accountId, transactionId));
    }

    /**
     * @param iban
     * @return
     * @deprecated: user request param instead
     */
    @Override
    @PreAuthorize("accountInfoByIban(#iban)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsByIban(String iban) {
        return ResponseEntity.ok(middlewareAccountService.getDepositAccountByIban(iban, LocalDateTime.now(), true));
    }

    @Override
    @PreAuthorize("accountInfoByIban(#request.psuAccount.iban)")
    public ResponseEntity<Boolean> fundsConfirmation(FundsConfirmationRequestTO request) {
        boolean fundsAvailable = middlewareAccountService.confirmFundsAvailability(request);
        return ResponseEntity.ok(fundsAvailable);
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> depositCash(String accountId, AmountTO amount) {
        middlewareAccountService.depositCash(scaInfoHolder.getScaInfo(), accountId, amount);
        return ResponseEntity.accepted().build();
    }

    private void dateChecker(LocalDate dateFrom, LocalDate dateTo) {
        if (!validDate(dateFrom).isEqual(validDate(dateTo))
                    && validDate(dateFrom).isAfter(validDate(dateTo))) {
            throw MiddlewareModuleException.builder()
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .devMsg("Illegal request dates sequence, possibly swapped 'date from' with 'date to'")
                          .build();
        }
    }

    private LocalDate validDate(LocalDate date) {
        return Optional.ofNullable(date)
                       .orElseGet(LocalDate::now);
    }
}
