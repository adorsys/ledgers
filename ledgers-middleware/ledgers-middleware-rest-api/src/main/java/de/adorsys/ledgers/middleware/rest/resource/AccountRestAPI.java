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

import de.adorsys.ledgers.middleware.api.domain.account.*;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
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

@Tag(name = "LDG003 - Accounts", description = "Provides access to a deposit account. This interface does not provide any endpoint to list all accounts.")
public interface AccountRestAPI {
    String BASE_PATH = "/accounts";
    String IBAN_QUERY_PARAM = "iban";
    String LOCAL_DATE_YYYY_MM_DD_FORMAT = "yyyy-MM-dd";
    String DATE_TO_QUERY_PARAM = "dateTo";
    String DATE_FROM_QUERY_PARAM = "dateFrom";
    String ACCOUNT_ID = "accountId";
    String TRANSACTION_ID = "transactionId";
    String PAGE = "page";
    String SIZE = "size";

    /**
     * Return the list of accounts linked with the current customer.
     *
     * @return : the list of accounts linked with the current customer.
     */
    @GetMapping
    @Operation(summary = "List fo Accessible Accounts",
            description = "Returns the list of all accounts linked to the connected user. "
                            + "Call only available to role CUSTOMER.")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDetailsTO.class)),
                    description = "List of accounts accessible to the user.")
    })
    ResponseEntity<List<AccountDetailsTO>> getListOfAccounts();

    @PostMapping
    @Operation(summary = "Registers a new Deposit Account",
            description = "Registers a new deposit account and assigns account access OWNER to the current user."
                            + "Following rules apply during and after registration of a new account:"
                            + "<ul>"
                            + "<li>Caller must have a role <b>CUSTOMER</b> this means STAFF and SYSTEM can not use this endpoint.</li>"
                            + "<li>Caller must have a valid <b>DIRECT_ACCESS</b> token. Means this can not be called using a LOGIN or a DELEGATED_ACCESS (tpp) token.</li>"
                            + "<li>The current access token of the user does not include the newly registered account. User must reauthenticate to obtain an updated access token.</li>"
                            + "<li>Nevertheless the Endpoint '/accounts' returns all accounts of the user.</li>"
                            + "<li>Endpoint for granting account access to another user is scheduled but not yet implemented.</li>"
                            + "</ul>")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account creation successful. Still planing to work with 201 here."),
            @ApiResponse(responseCode = "409", description = "Account with given IBAN already exists.")
    })
    ResponseEntity<Void> createDepositAccount(@RequestBody AccountDetailsTO accountDetailsTO);

    @GetMapping("/{accountId}")
    @Operation(summary = "Load Account by AccountId",
            description = "Returns account details information for the given account id. "
                            + "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDetailsTO.class)),
                    description = "Account details.")
    })
    ResponseEntity<AccountDetailsTO> getAccountDetailsById(@Parameter(name = ACCOUNT_ID) @PathVariable(name = "accountId") String accountId);

    @GetMapping("/{accountId}/balances")
    @Operation(summary = "Read balances",
            description = "Returns balances of the deposit account with the given accountId. "
                            + "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountBalanceTO.class)), description = "List of accounts balances for the given account.")
    })
    ResponseEntity<List<AccountBalanceTO>> getBalances(@Parameter(name = ACCOUNT_ID) @PathVariable(name = "accountId") String accountId);

    @GetMapping(path = "/{accountId}/transactions", params = {DATE_FROM_QUERY_PARAM, DATE_TO_QUERY_PARAM})
    @Operation(summary = "Find Transactions By Date", description = "Returns all transactions for the given account id")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    ResponseEntity<List<TransactionTO>> getTransactionByDates(
            @Parameter(name = ACCOUNT_ID)
            @PathVariable(name = "accountId") String accountId,
            @RequestParam(name = DATE_FROM_QUERY_PARAM, required = false) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateFrom,
            @RequestParam(name = DATE_TO_QUERY_PARAM) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateTo);

    @GetMapping(path = "/{accountId}/transactions/page", params = {DATE_FROM_QUERY_PARAM, DATE_TO_QUERY_PARAM, PAGE, SIZE})
    @Operation(summary = "Find Transactions By Date", description = "Returns transactions for the given account id for certain dates, paged view")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    ResponseEntity<CustomPageImpl<TransactionTO>> getTransactionByDatesPaged(
            @Parameter(name = ACCOUNT_ID)
            @PathVariable(name = "accountId") String accountId,
            @RequestParam(name = DATE_FROM_QUERY_PARAM, required = false) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateFrom,
            @RequestParam(name = DATE_TO_QUERY_PARAM) @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateTo,
            @RequestParam(PAGE) int page,
            @RequestParam(SIZE) int size);

    @GetMapping("/{accountId}/transactions/{transactionId}")
    @Operation(summary = "Load Transaction", description = "Returns the transaction with the given account id and transaction id.")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    ResponseEntity<TransactionTO> getTransactionById(
            @Parameter(name = ACCOUNT_ID)
            @PathVariable(name = "accountId") String accountId,
            @Parameter(name = TRANSACTION_ID)
            @PathVariable(name = "transactionId") String transactionId);

    /**
     * @param iban : the iban
     * @return : account details
     * @deprecated: user request param instead
     */
    @GetMapping(path = "/query", params = {IBAN_QUERY_PARAM})
    @Operation(summary = "Load Account Details By IBAN", description = "Returns account details information given the account IBAN")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    ResponseEntity<AccountDetailsTO> getAccountDetailsByIban(
            @Parameter(description = "The IBAN of the requested account: e.g.: DE69760700240340283600", example = "DE69760700240340283600")
            @RequestParam(name = IBAN_QUERY_PARAM) String iban);

    @Operation(summary = "Fund Confirmation", description = "Returns account details information given the account IBAN")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @PostMapping(value = "/funds-confirmation")
    ResponseEntity<Boolean> fundsConfirmation(
            @RequestBody FundsConfirmationRequestTO request);

    @PostMapping("/{accountId}/cash")
    @Operation(summary = "Deposit Cash", description = "Only technical users are authorized to perform this operation")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    ResponseEntity<Void> depositCash(@PathVariable(name = "accountId") String accountId, @RequestBody AmountTO amount);

    @GetMapping(path = "/info/{accountIdentifierType}/{accountIdentifier}")
    @Operation(summary = "Load Account Owner Additional information", description = "Returns Additional Account Information by Account Identifier")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    ResponseEntity<List<AdditionalAccountInformationTO>> getAdditionalAccountInfo(
            @Parameter(description = "Account identifier type i.e. ACCOUNT_ID / IBAN")
            @PathVariable(name = "accountIdentifierType") AccountIdentifierTypeTO accountIdentifierType,
            @Parameter(description = "The IBAN of the requested account: e.g.: DE69760700240340283600", example = "DE69760700240340283600")
            @PathVariable(name = "accountIdentifier") String accountIdentifier);
}
