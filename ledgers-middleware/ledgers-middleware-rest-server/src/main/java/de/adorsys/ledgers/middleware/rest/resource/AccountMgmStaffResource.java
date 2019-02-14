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
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.RestException;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Api(tags = "LDG008 - Accounts (STAFF access)" , description= "Provides access to the deposit account resource for staff members.")
@RestController
@RequestMapping("/staff/" + AccountRestAPI.BASE_PATH)
@MiddlewareUserResource
public class AccountMgmStaffResource {

	private static final Logger logger = LoggerFactory.getLogger(AccountMgmStaffResource.class);

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
     * @return response object with created account details
     */
    @ApiOperation(value="Registers a new Deposit Account for a user with specified ID",
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
            @ApiResponse(code=200, message="Account creation successful"),
            @ApiResponse(code=404, message="User with this ID not found"),
            @ApiResponse(code=403, message="User with this ID is not your branch"),
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
        return ResponseEntity.ok(middlewareAccountService.listOfDepositAccountsByBranch());
    }
}
