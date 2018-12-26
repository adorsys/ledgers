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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithPrefixGoneMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AccountWithSuffixExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.RestException;

@RestController
@RequestMapping(AccountRestAPI.BASE_PATH)
@SuppressWarnings("PMD.IdenticalCatchBranches")
@MiddlewareUserResource
public class AccountResource implements AccountRestAPI {

	private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

    private final MiddlewareAccountManagementService middlewareAccountService;

    public AccountResource(MiddlewareAccountManagementService middlewareAccountService) {
        this.middlewareAccountService = middlewareAccountService;
    }

    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsById(String accountId) {
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
    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<AccountBalanceTO>> getBalances(String accountId) {
    	return getBalances2(accountId);
    }

    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<AccountBalanceTO>> getBalances2(String accountId) {
        try {
            AccountDetailsTO accountDetails = middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true);
            return ResponseEntity.ok(accountDetails.getBalances());
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }
    
    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<TransactionTO> getTransactionById(String accountId, String transactionId) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getTransactionById(accountId, transactionId));
        } catch (AccountNotFoundMiddlewareException | TransactionNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }

    @Override
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<List<TransactionTO>> getTransactionByDates(String accountId,LocalDate dateFrom,LocalDate dateTo) {
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
    @Override
    @PreAuthorize("userLogin(#userLogin)")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccountDetailsByUserId(String userLogin) {
    	return getListOfAccountDetailsInternal(userLogin);
    }

    /**
     * Return the list of accounts linked with the current customer.
     * 
     * @return : the list of accounts linked with the current customer.
     */
    @Override
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
    @Override
    @PreAuthorize("accountInfoByIban(#iban)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsByIban(String iban) {
    	return getAccountDetailsByIban2(iban);
    }
    
    private ResponseEntity<AccountDetailsTO> getAccountDetailsByIban2(String iban) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getDepositAccountByIban(iban, LocalDateTime.now(), true));
        } catch (AccountNotFoundMiddlewareException e) {
            throw notFoundRestException(e);
        } catch (InsufficientPermissionMiddlewareException e) {
            throw forbiddenRestException(e);
		}
    }

    @Override
    @PreAuthorize("accountInfoByIban(#request.psuAccount.iban)")
    public ResponseEntity<Boolean> fundsConfirmation( FundsConfirmationRequestTO request) {
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
    
    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> createDepositAccount(AccountDetailsTO accountDetailsTO) {
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
