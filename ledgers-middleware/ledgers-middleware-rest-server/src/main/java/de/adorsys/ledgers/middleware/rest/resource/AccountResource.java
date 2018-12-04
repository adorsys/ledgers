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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.RestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/accounts")
@Api(tags = "Accounts" , description= "Provides access to a deposit account. This interface does not provide any endpoint to list all accounts.")
@SuppressWarnings("PMD.IdenticalCatchBranches")
public class AccountResource {
    private static final String THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY = "The id of the deposit account. Cannot be empty.";
    private static final String THE_ID_OF_THE_TRANSACTION_CANNOT_BE_EMPTY = "The id of the transaction. Cannot be empty.";

	private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

    private final MiddlewareAccountManagementService middlewareAccountService;

    public AccountResource(MiddlewareAccountManagementService middlewareAccountService) {
        this.middlewareAccountService = middlewareAccountService;
    }

    @GetMapping("/{accountId}")
    @ApiOperation("Returns account details information")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsById(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.MAX, true));
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
    @ApiOperation("Returns balances of the deposit account")
    public ResponseEntity<List<AccountBalanceTO>> getBalances(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId) {
    	return getBalances2(accountId);
    }

    @GetMapping("/{accountId}/balances")
    @ApiOperation("Returns balances of the deposit account with the given id")
    public ResponseEntity<List<AccountBalanceTO>> getBalances2(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId) {
        try {
            AccountDetailsTO accountDetails = middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.MAX, true);
            return ResponseEntity.ok(accountDetails.getBalances());
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }
    
    @GetMapping("{accountId}/transactions/{transactionId}")
    @ApiOperation("Returns the transaction with the given account id and transaction id.")
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

    @GetMapping("/{accountId}/transactions")
    @ApiOperation("Returns all transactions for the given account id")
    public ResponseEntity<List<TransactionTO>> getTransactionByDates(
    		@ApiParam(THE_ID_OF_THE_DEPOSIT_ACCOUNT_CANNOT_BE_EMPTY)
    		@PathVariable String accountId,
    		@RequestParam @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
    		@RequestParam @Nullable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateTo) {
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
    @ApiOperation("Returns the list of all accounts linked to the given user.")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccountDetailsByUserId(@PathVariable String userLogin) {
    	return getListOfAccountDetailsByUserLogin(userLogin);
    }

    private ResponseEntity<List<AccountDetailsTO>> getListOfAccountDetailsByUserLogin(String userLogin) {
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
    @GetMapping("/ibans/{iban}")
    @ApiOperation("Returns account details information given the account IBAN")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsByIban(
    		@ApiParam(value="The IBAN of the requested account: e.g.: DE69760700240340283600", example="DE69760700240340283600")
    		@PathVariable String iban) {
    	return getAccountDetailsByIban2(iban);
    }
    @GetMapping(params="iban")
    @ApiOperation("Returns account details information given the account IBAN")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsByIban2(
    		@ApiParam(value="The IBAN of the requested account: e.g.: DE69760700240340283600", example="DE69760700240340283600")
    		@RequestParam(required = true, name = "iban") String iban) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getDepositAccountByIban(iban, LocalDateTime.MAX, false));
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }

    @PostMapping(value = "/funds-confirmation")
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
}
