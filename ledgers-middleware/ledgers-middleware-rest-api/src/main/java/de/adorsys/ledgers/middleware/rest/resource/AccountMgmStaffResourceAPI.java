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
import de.adorsys.ledgers.middleware.api.domain.account.AccountReportTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "LDG011 - Accounts (STAFF access)", description = "Provides access to the deposit account resource for staff members.")
public interface AccountMgmStaffResourceAPI {
    String BASE_PATH = "/staff-access" + AccountRestAPI.BASE_PATH;
    String ACCOUNT_ID = "accountId";
    String USER_ID = "userId";
    String QUERY_PARAM = "queryParam";
    String PAGE = "page";
    String SIZE = "size";

    @Operation(summary = "Retrieves account by iban and Currency")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode ="200", description = "Account creation successful"),
            @ApiResponse(responseCode ="404", description = "User with this ID not found"),
            @ApiResponse(responseCode ="409", description = "Account with given IBAN already exists.")
    })
    @GetMapping("/acc/acc")
    ResponseEntity<List<AccountDetailsTO>> getAccountsByIbanAndCurrency(@RequestParam(name = "iban") String iban, @RequestParam(name = "currency", required = false, defaultValue = "") String currency);

    /**
     * Creates a new deposit account for a user specified by ID
     * Account is created for the same branch as Staff user
     *
     * @param userId           user for who account is created
     * @param accountDetailsTO account details
     * @return Void
     */
    @Operation(summary = "Registers a new Deposit Account for a user with specified ID",
            description = "Registers a new deposit account and assigns account access OWNER to the current user.")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode ="200", description = "Account creation successful"),
            @ApiResponse(responseCode ="404", description = "User with this ID not found"),
            @ApiResponse(responseCode ="409", description = "Account with given IBAN already exists.")
    })
    @PostMapping
    ResponseEntity<Void> createDepositAccountForUser(@RequestParam(name = USER_ID) String userId, @RequestBody AccountDetailsTO accountDetailsTO);

    /**
     * Returns the list of accounts that belong to the same branch as STAFF user.
     *
     * @return list of accounts that belongs to the same branch as staff user.
     */
    @Operation(summary = "List fo Accessible Accounts",
            description = "Returns the list of all accounts linked to the connected user. "
                            + "Call only available to role CUSTOMER.")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDetailsTO[].class)), description = "List of accounts accessible to the user.")
    })
    @GetMapping
    ResponseEntity<List<AccountDetailsTO>> getListOfAccounts();

    @Operation(summary = "List fo Accessible Accounts",
            description = "Returns the list of all accounts linked to the connected user, paged view. "
                            + "Call only available to role CUSTOMER.")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDetailsTO[].class)), description = "List of accounts accessible to the user.")
    })
    @GetMapping(path = "/page")
    ResponseEntity<CustomPageImpl<AccountDetailsTO>> getListOfAccountsPaged(
            @RequestParam(value = QUERY_PARAM, defaultValue = "", required = false) String queryParam,
            @RequestParam(PAGE) int page, @RequestParam(SIZE) int size);

    @Operation(summary = "Load Account by AccountId",
            description = "Returns account details information for the given account id. "
                            + "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AccountDetailsTO.class)), description = "Account details.")
    })
    @GetMapping("/{accountId}")
    ResponseEntity<AccountDetailsTO> getAccountDetailsById(@Parameter(name = ACCOUNT_ID) @PathVariable(ACCOUNT_ID) String accountId);

    /**
     * Operation deposits cash to the deposit account
     *
     * @param accountId Account ID in Ledgers
     * @param amount    Amount to be deposited
     * @return Void
     */
    @Operation(summary = "Deposit Cash",
            description = "Operation for staff member to register cash in the deposit account")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Operation was successful")
    })
    @PostMapping("/{accountId}/cash")
    ResponseEntity<Void> depositCash(@PathVariable(ACCOUNT_ID) String accountId, @RequestBody AmountTO amount);

    @Operation(summary = "Load Extended Account Details by AccountId",
            description = "Returns extended account details information for the given account id. "
                            + "User must have access to the target account. This is also accessible to other token types like tpp token (DELEGATED_ACESS)")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = AccountReportTO.class)),
                    description = "Extended Account details.")
    })
    @GetMapping("/{accountId}/extended")
    ResponseEntity<AccountReportTO> getExtendedAccountDetailsById(@Parameter(name = ACCOUNT_ID) @PathVariable(ACCOUNT_ID) String accountId);

    @Operation(summary = "Block/Unblock account",
            description = "Changes block state for given account, returns status being set to the block")
    @SecurityRequirement(name = "apiKey")
    @SecurityRequirement(name = "oAuth2")
    @PostMapping("/{accountId}/status")
    ResponseEntity<Boolean> changeStatus(@PathVariable(ACCOUNT_ID) String accountId);

}
