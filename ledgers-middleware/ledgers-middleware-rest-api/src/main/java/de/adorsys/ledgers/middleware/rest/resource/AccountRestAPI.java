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

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

@Api(tags = "Accounts" , description= "Provides access to a deposit account. This interface does not provide any endpoint to list all accounts.")
public interface AccountRestAPI {
	public static final String IBANS_IBAN_PARAM = "/ibans/{iban}";
	public static final String LIST_OF_ACCOUNTS_PATH = "/listOfAccounts";
	public static final String LOCAL_DATE_YYYY_MM_DD_FORMAT = "yyyy-MM-dd";
	public static final String DATE_TO_QUERY_PARAM = "dateTo";
	public static final String DATE_FROM_QUERY_PARAM = "dateFrom";
	public static final String ACCOUNT_ID__TRANSACTIONS_PATH = "/{accountId}/transactions";
	public static final String BASE_PATH = "/accounts";
	public static final String THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY = "The id of the deposit account. Cannot be empty.";
    public static final String THE_ID_OF_THE_TRANSACTION_CANNOT_BE_EMPTY = "The id of the transaction. Cannot be empty.";

    @GetMapping("/{accountId}")
    @ApiOperation(value="Load Account by AccountIs", 
    	notes="Returns account details information", 
    	authorizations =@Authorization(value="apiKey"))
    ResponseEntity<AccountDetailsTO> getAccountDetailsById(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable(name="accountId") String accountId) throws NotFoundRestException, ForbiddenRestException;

    /**
     * @deprecated : wrong REST principles applied here
     * 
     * @param accountId
     * @return
     */
    @GetMapping("/balances/{accountId}")
    @ApiOperation(value="Get Balances", notes="Returns balances of the deposit account", 
    	authorizations =@Authorization(value="apiKey"))
    ResponseEntity<List<AccountBalanceTO>> getBalances(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable(name="accountId") String accountId) throws NotFoundRestException, ForbiddenRestException;

    @GetMapping("/{accountId}/balances")
    @ApiOperation(value="Returns balances of the deposit account with the given id", authorizations =@Authorization(value="apiKey"))
    ResponseEntity<List<AccountBalanceTO>> getBalances2(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable(name="accountId") String accountId) throws NotFoundRestException, ForbiddenRestException;
    
    @GetMapping("{accountId}/transactions/{transactionId}")
    @ApiOperation(value="Load Transaction", notes="Returns the transaction with the given account id and transaction id.", 
    	authorizations =@Authorization(value="apiKey"))
    ResponseEntity<TransactionTO> getTransactionById(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable(name="accountId") String accountId, 
    		@ApiParam(THE_ID_OF_THE_TRANSACTION_CANNOT_BE_EMPTY)
    		@PathVariable(name="transactionId") String transactionId)  throws NotFoundRestException, ForbiddenRestException;

    @GetMapping(path=ACCOUNT_ID__TRANSACTIONS_PATH, params= {DATE_FROM_QUERY_PARAM,DATE_TO_QUERY_PARAM})
    @ApiOperation(value="Find Transactions By Date", notes="Returns all transactions for the given account id", 
    	authorizations =@Authorization(value="apiKey"))
    ResponseEntity<List<TransactionTO>> getTransactionByDates(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable(name="accountId") String accountId,
    		@RequestParam(name=DATE_FROM_QUERY_PARAM) @Nullable @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateFrom,
    		@RequestParam(name=DATE_TO_QUERY_PARAM) @Nullable @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateTo)
    				 throws NotFoundRestException, ForbiddenRestException;

    /**
     * TODO: Bad REST design. Use query parameter instead.
     * 
     * @deprecated: Access list of accounts thru the user resource and use the iban to read account details.
     * @param userLogin
     * @return
     */
    @GetMapping("/users/{userLogin}")
    @ApiOperation(value="Load Accounts By User Login", notes="Returns the list of all accounts linked to the given user.", 
    	authorizations =@Authorization(value="apiKey"))
    ResponseEntity<List<AccountDetailsTO>> getListOfAccountDetailsByUserId(
    		@PathVariable(name="userLogin") String userLogin)
    		 throws NotFoundRestException, ForbiddenRestException;

    /**
     * Return the list of accounts linked with the current customer.
     * 
     * @return : the list of accounts linked with the current customer.
     */
    @GetMapping(path=LIST_OF_ACCOUNTS_PATH)
    @ApiOperation(value="List Accounts", authorizations =@Authorization(value="apiKey"), notes="Returns the list of all accounts linked to the connected user. Call only available to customer.")
    ResponseEntity<List<AccountDetailsTO>> getListOfAccounts()  throws ForbiddenRestException;

    /**
     * @deprecated: user request param instead
     * @param iban
     * @return
     */
    @GetMapping(IBANS_IBAN_PARAM)
    @ApiOperation(value="Load Account Details By IBAN", authorizations =@Authorization(value="apiKey"), notes="Returns account details information given the account IBAN")
    ResponseEntity<AccountDetailsTO> getAccountDetailsByIban(
    		@ApiParam(value="The IBAN of the requested account: e.g.: DE69760700240340283600", example="DE69760700240340283600")
    		@PathVariable(name="iban") String iban)  throws NotFoundRestException, ForbiddenRestException;

    @ApiOperation(value="Fund Confirmation", authorizations =@Authorization(value="apiKey"), notes="Returns account details information given the account IBAN")
    @PostMapping(value = "/funds-confirmation")
    ResponseEntity<Boolean> fundsConfirmation(
    		@RequestBody FundsConfirmationRequestTO request) 
    		 throws NotFoundRestException, ForbiddenRestException;


    @PostMapping("/")
    @ApiOperation(value="Create Deposit Account", authorizations =@Authorization(value="apiKey"), notes="Creates a deposit account")
    ResponseEntity<Void> createDepositAccount(
    		@RequestBody AccountDetailsTO accountDetailsTO) 
    		 throws ForbiddenRestException, ConflictRestException;
}
