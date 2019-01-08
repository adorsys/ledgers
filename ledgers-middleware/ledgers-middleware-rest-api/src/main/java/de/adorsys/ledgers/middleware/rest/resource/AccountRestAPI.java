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

import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

@Api(tags = "LDG002 - Accounts" , description= "Provides access to a deposit account. This interface does not provide any endpoint to list all accounts.")
public interface AccountRestAPI {
	String BASE_PATH = "/accounts";
	String IBAN_QUERY_PARAM = "iban";
	String LOCAL_DATE_YYYY_MM_DD_FORMAT = "yyyy-MM-dd";
	String DATE_TO_QUERY_PARAM = "dateTo";
	String DATE_FROM_QUERY_PARAM = "dateFrom";
	String ACCOUNT_ID = "accountId";
    String TRANSACTION_ID = "transactionId";
	String THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY = "The id of the deposit account. Cannot be empty.";
    String THE_ID_OF_THE_TRANSACTION_CANNOT_BE_EMPTY = "The id of the transaction. Cannot be empty.";

    /**
     * Return the list of accounts linked with the current customer.
     * 
     * @return : the list of accounts linked with the current customer.
     */
    @GetMapping
    @ApiOperation(value="List fo Accessible Accounts", authorizations =@Authorization(value="apiKey"), 
    	notes="Returns the list of all accounts linked to the connected user. "
    			+ "Call only available to role CUSTOMER.")
	@ApiResponses(value={
			@ApiResponse(code=200, response=AccountDetailsTO[].class, message="List of accounts accessible to the user.")
		})
    ResponseEntity<List<AccountDetailsTO>> getListOfAccounts()  throws ForbiddenRestException;

    @PostMapping
	@ApiOperation(value="Registers a new Deposit Account", 
	notes="Registers a new deposit account and assigns account access OWNER to the current user."
			+ "Following rules apply during and after registration of a new account:"
			+ "<ul>"
				+ "<li>Caller must have a role <b>CUSTOMER</b> this means STAFF and SYSTEM can not use this endpoint.</li>"
				+ "<li>Caller must have a valid <b>DIRECT_ACCESS</b> token. Means this can not be called using a LOGIN or a DELEGATED_ACCESS (tpp) token.</li>"
				+ "<li>The current access token of the user does not include the newly registered account. User must reauthenticate to obtain an updated access token.</li>"
				+ "<li>Nevertheless the Endpoint '/accounts' returns all accounts of the user.</li>"
				+ "<li>Endpoint for granting account access to another user is scheduled but not yet implemented.</li>"
			+ "</ul>", 
	authorizations =@Authorization(value="apiKey"))
	@ApiResponses(value={
		@ApiResponse(code=200, response=Void.class, message="Account creation successfull. Still planing to work with 201 here."),
		@ApiResponse(code=409, message="Account with given IBAN already exists.")
	})
    ResponseEntity<Void> createDepositAccount(
    		@RequestBody AccountDetailsTO accountDetailsTO) 
    		 throws ForbiddenRestException, ConflictRestException;
    
    @GetMapping("/{accountId}")
    @ApiOperation(value="Load Account by AccountId", 
    	notes="Returns account details information for the given account id. "
    			+ "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)", 
    	authorizations =@Authorization(value="apiKey"))
	@ApiResponses(value={
			@ApiResponse(code=200, response=AccountDetailsTO.class, message="Account details.")
		})
    ResponseEntity<AccountDetailsTO> getAccountDetailsById(
    		@ApiParam(ACCOUNT_ID)
    		@PathVariable(name="accountId") String accountId) throws NotFoundRestException, ForbiddenRestException;

    @GetMapping("/{accountId}/balances")
    @ApiOperation(value="Read balances", 
    	notes="Returns balances of the deposit account with the given accountId. "
    			+ "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)",
    	authorizations =@Authorization(value="apiKey"))
	@ApiResponses(value={
			@ApiResponse(code=200, response=AccountBalanceTO[].class, message="List of accounts balances for the given account.")
		})
    ResponseEntity<List<AccountBalanceTO>> getBalances(
    		@ApiParam(ACCOUNT_ID)
    		@PathVariable(name="accountId") String accountId) throws NotFoundRestException, ForbiddenRestException;
    
    @GetMapping(path="/{accountId}/transactions", params= {DATE_FROM_QUERY_PARAM,DATE_TO_QUERY_PARAM})
    @ApiOperation(value="Find Transactions By Date", notes="Returns all transactions for the given account id", 
    	authorizations =@Authorization(value="apiKey"))
    ResponseEntity<List<TransactionTO>> getTransactionByDates(
    		@ApiParam(ACCOUNT_ID)
    		@PathVariable(name="accountId") String accountId,
    		@RequestParam(name=DATE_FROM_QUERY_PARAM) @Nullable @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateFrom,
    		@RequestParam(name=DATE_TO_QUERY_PARAM) @Nullable @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateTo)
    				 throws NotFoundRestException, ForbiddenRestException;

    @GetMapping("/{accountId}/transactions/{transactionId}")
    @ApiOperation(value="Load Transaction", notes="Returns the transaction with the given account id and transaction id.", 
    	authorizations =@Authorization(value="apiKey"))
    ResponseEntity<TransactionTO> getTransactionById(
    		@ApiParam(ACCOUNT_ID)
    		@PathVariable(name="accountId") String accountId, 
    		@ApiParam(TRANSACTION_ID)
    		@PathVariable(name="transactionId") String transactionId)  throws NotFoundRestException, ForbiddenRestException;

    /**
     * @deprecated: user request param instead
     * @param iban : the iban
     * @return : account details
     */
    @GetMapping(path="/query", params= {IBAN_QUERY_PARAM})
    @ApiOperation(value="Load Account Details By IBAN", authorizations =@Authorization(value="apiKey"), notes="Returns account details information given the account IBAN")
    ResponseEntity<AccountDetailsTO> getAccountDetailsByIban(
    		@ApiParam(value="The IBAN of the requested account: e.g.: DE69760700240340283600", example="DE69760700240340283600")
    		@RequestParam(name=IBAN_QUERY_PARAM) String iban)  throws NotFoundRestException, ForbiddenRestException;

    @ApiOperation(value="Fund Confirmation", authorizations =@Authorization(value="apiKey"), notes="Returns account details information given the account IBAN")
    @PostMapping(value = "/funds-confirmation")
    ResponseEntity<Boolean> fundsConfirmation(
    		@RequestBody FundsConfirmationRequestTO request) 
    		 throws NotFoundRestException, ForbiddenRestException;

	@PostMapping("/{accountId}/cash")
	@ApiOperation(value="Deposit Cash", authorizations=@Authorization(value="apiKey"), notes = "Only technical users are authorized to perform this operation")
	ResponseEntity<Void> depositCash(@PathVariable(name="accountId") String accountId, @RequestBody AmountTO amount);
}
