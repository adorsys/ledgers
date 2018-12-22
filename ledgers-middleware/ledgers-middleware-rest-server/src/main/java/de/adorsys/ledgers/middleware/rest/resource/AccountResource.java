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
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.RestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(AccountResource.BASE_PATH)
@Api(tags = "Accounts" , description= "Provides access to a deposit account. This interface does not provide any endpoint to list all accounts.")
@SuppressWarnings("PMD.IdenticalCatchBranches")
@MiddlewareUserResource
public class AccountResource {
	public static final String IBANS_IBAN_PARAM = "/ibans/{iban}";
	public static final String LIST_OF_ACCOUNTS_PATH = "/listOfAccounts";
	public static final String LOCAL_DATE_YYYY_MM_DD_FORMAT = "yyyy-MM-dd";
	public static final String DATE_TO_QUERY_PARAM = "dateTo";
	public static final String DATE_FROM_QUERY_PARAM = "dateFrom";
	public static final String ACCOUNT_ID__TRANSACTIONS_PATH = "/{accountId}/transactions";
	public static final String BASE_PATH = "/accounts";
//	public static final String IBAN_QUERY_PARAM = "iban";
	private static final String THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY = "The id of the deposit account. Cannot be empty.";
    private static final String THE_ID_OF_THE_TRANSACTION_CANNOT_BE_EMPTY = "The id of the transaction. Cannot be empty.";

	private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

    private final MiddlewareAccountManagementService middlewareAccountService;

    public AccountResource(MiddlewareAccountManagementService middlewareAccountService) {
        this.middlewareAccountService = middlewareAccountService;
    }

    @GetMapping("/{accountId}")
    @ApiOperation(value="Load Account by AccountIs", notes="Returns account details information", authorizations =@Authorization(value="apiKey"))
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsById(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true));
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }

	private RestException forbiddenRestException(InsufficientPermissionMiddlewareException e) {
		logger.error(e.getMessage(), e);
		return new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
	}

	private RestException notFoundRestException(AccountNotFoundMiddlewareException e) {
		logger.error(e.getMessage(), e);
		return new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
	}

    /**
     * @deprecated : wrong REST principles applied here
     * 
     * @param accountId
     * @return
     */
    @GetMapping("/balances/{accountId}")
    @ApiOperation(value="Get Balances", notes="Returns balances of the deposit account", authorizations =@Authorization(value="apiKey"))
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<AccountBalanceTO>> getBalances(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId) {
    	return getBalances2(accountId);
    }

    @GetMapping("/{accountId}/balances")
    @ApiOperation("Returns balances of the deposit account with the given id")
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<AccountBalanceTO>> getBalances2(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId) {
        try {
            AccountDetailsTO accountDetails = middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true);
            return ResponseEntity.ok(accountDetails.getBalances());
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }
    
    @GetMapping("{accountId}/transactions/{transactionId}")
    @ApiOperation(value="Load Transaction", notes="Returns the transaction with the given account id and transaction id.", authorizations =@Authorization(value="apiKey"))
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<TransactionTO> getTransactionById(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId, 
    		@ApiParam(THE_ID_OF_THE_TRANSACTION_CANNOT_BE_EMPTY)
    		@PathVariable String transactionId) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getTransactionById(accountId, transactionId));
        } catch (AccountNotFoundMiddlewareException | TransactionNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }

    @GetMapping(path=ACCOUNT_ID__TRANSACTIONS_PATH, params= {DATE_FROM_QUERY_PARAM,DATE_TO_QUERY_PARAM})
    @ApiOperation(value="Find Transactions By Date", notes="Returns all transactions for the given account id", authorizations =@Authorization(value="apiKey"))
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<TransactionTO>> getTransactionByDates(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId,
    		@RequestParam(name=DATE_FROM_QUERY_PARAM) @Nullable @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateFrom,
    		@RequestParam(name=DATE_TO_QUERY_PARAM) @Nullable @DateTimeFormat(pattern = LOCAL_DATE_YYYY_MM_DD_FORMAT) LocalDate dateTo) {
        dateChecker(dateFrom, dateTo);
        try {
            List<TransactionTO> transactions = middlewareAccountService.getTransactionsByDates(accountId, validDate(dateFrom), validDate(dateTo));
            return ResponseEntity.ok(transactions);
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }

    /**
     * TODO: Bad REST design. Use query parameter instead.
     * 
     * @deprecated: Access list of accounts thru the user resource and use the iban to read account details.
     * @param userLogin
     * @return
     */
    @GetMapping("/users/{userLogin}")
    @ApiOperation(value="Load Accounts By User Login", notes="Returns the list of all accounts linked to the given user.", authorizations =@Authorization(value="apiKey"))
    @PreAuthorize("userLogin(#userLogin)")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccountDetailsByUserId(@PathVariable String userLogin) {
    	return getListOfAccountDetailsInternal(userLogin);
    }

    /**
     * Return the list of accounts linked with the current customer.
     * 
     * @return : the list of accounts linked with the current customer.
     */
    @GetMapping(path=LIST_OF_ACCOUNTS_PATH)
    @ApiOperation(value="List Accounts", authorizations =@Authorization(value="apiKey"), notes="Returns the list of all accounts linked to the connected user. Call only available to customer.")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccounts() {
        return ResponseEntity.ok(middlewareAccountService.listOfDepositAccounts());
    }
    
    private ResponseEntity<List<AccountDetailsTO>> getListOfAccountDetailsInternal(String userLogin) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getAllAccountDetailsByUserLogin(userLogin));
        } catch (UserNotFoundMiddlewareException | AccountNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
		}
    }
    
    /**
     * @deprecated: user request param instead
     * @param iban
     * @return
     */
    @GetMapping(IBANS_IBAN_PARAM)
    @ApiOperation(value="Load Account Details By IBAN", authorizations =@Authorization(value="apiKey"), notes="Returns account details information given the account IBAN")
    @PreAuthorize("accountInfoByIban(#iban)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsByIban(
    		@ApiParam(value="The IBAN of the requested account: e.g.: DE69760700240340283600", example="DE69760700240340283600")
    		@PathVariable String iban) {
    	return getAccountDetailsByIban2(iban);
    }
    
//    @GetMapping(path="/byIban" params=IBAN_QUERY_PARAM)
//    @PreAuthorize("accountInfoByIban(#iban)")
    private ResponseEntity<AccountDetailsTO> getAccountDetailsByIban2(String iban) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getDepositAccountByIban(iban, LocalDateTime.now(), true));
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }

    @ApiOperation(value="Fund Confirmation", authorizations =@Authorization(value="apiKey"), notes="Returns account details information given the account IBAN")
    @PostMapping(value = "/funds-confirmation")
    @PreAuthorize("accountInfoByIban(#request.psuAccount.iban)")
    public ResponseEntity<Boolean> fundsConfirmation(@RequestBody FundsConfirmationRequestTO request) {
        try {
            boolean fundsAvailable = middlewareAccountService.confirmFundsAvailability(request);
            return ResponseEntity.ok(fundsAvailable);
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        }
    }
    
    

    private void dateChecker(LocalDate dateFrom, LocalDate dateTo) {
        if (!validDate(dateFrom).isEqual(validDate(dateTo))
                    && validDate(dateFrom).isAfter(validDate(dateTo))) {
            throw new ConflictRestException("Illegal request dates sequence, possibly swapped 'date from' with 'date to'");
        }
    }

    private LocalDate validDate(LocalDate date) {
        return Optional.ofNullable(date)
                       .orElseGet(LocalDate::now);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ApiOperation(value="Create Deposit Account", authorizations =@Authorization(value="apiKey"), notes="Creates a deposit account")
    public ResponseEntity<Void> createDepositAccount(@RequestBody AccountDetailsTO accountDetailsTO) {
		// create account. It does not exist.
		String iban = accountDetailsTO.getIban();
		// Splitt in prefix and suffix
		String accountNumberPrefix = StringUtils.substring(iban, 0, iban.length()-2);
		String accountNumberSuffix = StringUtils.substringAfter(iban, accountNumberPrefix);
    	
    	try {
			middlewareAccountService.createDepositAccount(accountNumberPrefix, accountNumberSuffix, accountDetailsTO);
            return ResponseEntity.ok().build();
		} catch (AccountWithPrefixGoneMiddlewareException | AccountWithSuffixExistsMiddlewareException | UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
		}
    }
}
