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

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotInBranchMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Api(tags = "LDG008 - Accounts (STAFF access)" , description= "Provides access to the deposit account resource for staff members.")
@RestController
@RequestMapping("/staff-access/" + AccountRestAPI.BASE_PATH)
@MiddlewareUserResource
public class AccountMgmStaffResource {

	private static final Logger logger = LoggerFactory.getLogger(AccountMgmStaffResource.class);
    private static final String ACCOUNT_ID = "accountId";
    private final MiddlewareAccountManagementService middlewareAccountService;

    public AccountMgmStaffResource(MiddlewareAccountManagementService middlewareAccountService) {
        this.middlewareAccountService = middlewareAccountService;
    }

    /**
     * Creates a new deposit account for a user specified by ID
     * Account is created for the same branch as Staff user
     *
     * @param userID user for who account is created
     * @param accountDetailsTO account details
     * @return Void
     */
    @ApiOperation(value="Registers a new Deposit Account for a user with specified ID",
            notes="Registers a new deposit account and assigns account access OWNER to the current user.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, message="Account creation successful"),
            @ApiResponse(code=404, message="User with this ID not found"),
            @ApiResponse(code=409, message="Account with given IBAN already exists.")
    })
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> createDepositAccountForUser(@RequestParam String userID, @RequestBody AccountDetailsTO accountDetailsTO) {
        try {
            middlewareAccountService.createDepositAccount(userID, accountDetailsTO);

            // TODO: change to created after Account Middleware service refactoring
            return ResponseEntity.ok().build();
        } catch (UserNotFoundMiddlewareException e) {
            return ResponseEntity.notFound().build();
        } catch (UserNotInBranchMiddlewareException e) {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * Returns the list of accounts that belong to the same branch as STAFF user.
     *
     * @return list of accounts that belongs to the same branch as staff user.
     */
    @ApiOperation(value="List fo Accessible Accounts", authorizations =@Authorization(value="apiKey"),
            notes="Returns the list of all accounts linked to the connected user. "
                    + "Call only available to role CUSTOMER.")
    @ApiResponses(value={
            @ApiResponse(code=200, response=AccountDetailsTO[].class, message="List of accounts accessible to the user.")
    })
    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<AccountDetailsTO>> getListOfAccounts() {
        return ResponseEntity.ok(middlewareAccountService.listDepositAccountsByBranch());
    }


    @ApiOperation(value="Load Account by AccountId",
            notes="Returns account details information for the given account id. "
                    + "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=AccountDetailsTO.class, message="Account details.")
    })
    @GetMapping("/{accountId}")
    @PreAuthorize("accountInfoById(#accountId)")
    public ResponseEntity<AccountDetailsTO> getAccountDetailsById(@ApiParam(ACCOUNT_ID) @PathVariable String accountId) {
        try {
            return ResponseEntity.ok(middlewareAccountService.getDepositAccountById(accountId, LocalDateTime.now(), true));
        } catch (AccountNotFoundMiddlewareException | InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    /**
     * Operation deposits cash to the deposit account
     *
     * @param accountId Account ID in Ledgers
     * @param amount Amount to be deposited
     * @return Void
     */
    @ApiOperation(value="Deposit Cash", authorizations=@Authorization(value="apiKey"),
            notes = "Operation for staff member to register cash in the deposit account")
    @ApiResponses(value={
            @ApiResponse(code=202, message="Operation was successful")
    })
    @PostMapping("/{accountId}/cash")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> depositCash(@PathVariable String accountId, @RequestBody AmountTO amount) {
        try {
            middlewareAccountService.depositCash(accountId, amount);
            return ResponseEntity.accepted().build();
        } catch (AccountNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }
}
