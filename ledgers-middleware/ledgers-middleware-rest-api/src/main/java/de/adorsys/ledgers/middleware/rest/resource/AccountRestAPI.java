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
import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.AdditionalAccountInformationTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG003 - Accounts", description = "Provides access to a deposit account. This interface does not provide any endpoint to list all accounts.")
public interface AccountRestAPI {
    String BASE_PATH = "/accounts";

    /**
     * Return the list of accounts linked with the current customer.
     *
     * @return : the list of accounts linked with the current customer.
     */
    @GetMapping
    @Operation(summary = "List fo Accessible Accounts",
            description = "Returns the list of all accounts linked to the connected user. "
                                  + "Call only available to role CUSTOMER.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDetailsTO.class)),
                    description = "List of accounts accessible to the user.")
    })
    ResponseEntity<List<AccountDetailsTO>> getListOfAccounts();

    @GetMapping("/{accountId}")
    @Operation(summary = "Load Account by AccountId",
            description = "Returns account details information for the given account id. "
                                  + "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDetailsTO.class)),
                    description = "Account details.")
    })
    ResponseEntity<AccountDetailsTO> getAccountDetailsById(@Parameter(name = ACCOUNT_ID) @PathVariable(ACCOUNT_ID) String accountId);

    @GetMapping("/{accountId}/balances")
    @Operation(summary = "Read balances",
            description = "Returns balances of the deposit account with the given accountId. "
                                  + "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountBalanceTO.class)), description = "List of accounts balances for the given account.")
    })
    ResponseEntity<List<AccountBalanceTO>> getBalances(@Parameter(name = ACCOUNT_ID) @PathVariable(ACCOUNT_ID) String accountId);

    @GetMapping(path = "/{accountId}/transactions", params = {DATE_FROM_QUERY_PARAM, DATE_TO_QUERY_PARAM})
    @Operation(summary = "Find Transactions By Date", description = "Returns all transactions for the given account id")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<List<TransactionTO>> getTransactionByDates(
            @Parameter(name = ACCOUNT_ID)
            @PathVariable(ACCOUNT_ID) String accountId,
            @RequestParam(name = DATE_FROM_QUERY_PARAM, required = false) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateFrom,
            @RequestParam(name = DATE_TO_QUERY_PARAM) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateTo);

    @GetMapping(path = "/{accountId}/transactions/page", params = {DATE_FROM_QUERY_PARAM, DATE_TO_QUERY_PARAM, PAGE, SIZE})
    @Operation(summary = "Find Transactions By Date", description = "Returns transactions for the given account id for certain dates, paged view")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<CustomPageImpl<TransactionTO>> getTransactionByDatesPaged(
            @Parameter(name = ACCOUNT_ID)
            @PathVariable(name = ACCOUNT_ID) String accountId,
            @RequestParam(name = DATE_FROM_QUERY_PARAM, required = false) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateFrom,
            @RequestParam(name = DATE_TO_QUERY_PARAM) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateTo,
            @RequestParam(PAGE) int page,
            @RequestParam(SIZE) int size);

    @GetMapping("/{accountId}/transactions/{transactionId}")
    @Operation(summary = "Load Transaction", description = "Returns the transaction with the given account id and transaction id.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<TransactionTO> getTransactionById(
            @Parameter(name = ACCOUNT_ID)
            @PathVariable(name = ACCOUNT_ID) String accountId,
            @Parameter(name = TRANSACTION_ID)
            @PathVariable(name = TRANSACTION_ID) String transactionId);

    @Operation(summary = "Fund Confirmation", description = "Returns account details information given the account IBAN")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PostMapping(value = "/funds-confirmation")
    ResponseEntity<Boolean> fundsConfirmation(
            @RequestBody FundsConfirmationRequestTO request);

    @GetMapping(path = "/info/{accountIdentifierType}/{accountIdentifier}")
    @Operation(summary = "Load Account Owner Additional information", description = "Returns Additional Account Information by Account Identifier")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<List<AdditionalAccountInformationTO>> getAdditionalAccountInfo(
            @Parameter(description = "Account identifier type i.e. ACCOUNT_ID / IBAN")
            @PathVariable(name = ACCOUNT_IDENTIFIER_TYPE) AccountIdentifierTypeTO accountIdentifierType,
            @Parameter(description = "The IBAN of the requested account: e.g.: DE69760700240340283600", example = "DE69760700240340283600")
            @PathVariable(name = ACCOUNT_IDENTIFIER) String accountIdentifier);
}
