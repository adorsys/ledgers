/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsExtendedTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserExtendedTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG013 - Admin Management (SYSTEM access)")
public interface AdminResourceAPI {
    String BASE_PATH = "/admin";

    @Operation(summary = "Lists all users, un-paged")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @GetMapping("users/all")
    ResponseEntity<List<UserTO>> getAllUsers();

    @Operation(summary = "Get users with filtering",
            description = "Retrieves Page of Users with multiple filters")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @GetMapping("/users")
    ResponseEntity<CustomPageImpl<UserExtendedTO>> users(@RequestParam(value = COUNTRY_CODE, defaultValue = "", required = false) String countryCode,
                                                         @RequestParam(value = BRANCH_ID, defaultValue = "", required = false) String branchId,
                                                         @RequestParam(value = BRANCH_LOGIN, defaultValue = "", required = false) String branchLogin,
                                                         @RequestParam(value = USER_LOGIN, defaultValue = "", required = false) String userLogin,
                                                         @RequestParam(value = ROLE, required = false) UserRoleTO role,
                                                         @RequestParam(value = BLOCKED, required = false) Boolean blocked,
                                                         @RequestParam(PAGE) int page,
                                                         @RequestParam(SIZE) int size);

    @Operation(summary = "Get users with System role",
            description = "Retrieves Page of Users with System role")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @GetMapping("/admins")
    ResponseEntity<CustomPageImpl<UserTO>> admins(@RequestParam(PAGE) int page,
                                                  @RequestParam(SIZE) int size);

    @Operation(summary = "Get accounts with filtering",
            description = "Retrieves Page of Accounts with multiple filters")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @GetMapping("/accounts")
    ResponseEntity<CustomPageImpl<AccountDetailsExtendedTO>> accounts(@RequestParam(value = COUNTRY_CODE, defaultValue = "", required = false) String countryCode,
                                                                      @RequestParam(value = BRANCH_ID, defaultValue = "", required = false) String branchId,
                                                                      @RequestParam(value = BRANCH_LOGIN, defaultValue = "", required = false) String branchLogin,
                                                                      @RequestParam(value = IBAN, required = false, defaultValue = "") String iban,
                                                                      @RequestParam(value = BLOCKED, required = false) Boolean blocked,
                                                                      @RequestParam(PAGE) int page,
                                                                      @RequestParam(SIZE) int size);

    @Operation(summary = "Set password for Branch",
            description = "Changes password for Branch")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PutMapping("/password")
    ResponseEntity<Void> updatePassword(@RequestParam(value = BRANCH_ID) String branchId,
                                        @RequestParam(PASSWORD) String password);

    @Operation(summary = "Block/Unblock user",
            description = "Changes system block or regular block state for given user, returns status being set to the block")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PostMapping("/status")
    ResponseEntity<Boolean> changeStatus(@RequestParam(value = USER_ID) String userId);

    @Operation(summary = "Create new User by Admin",
            description = "Can create STAFF/CUSTOMER/SYSTEM users")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PostMapping("/user")
    ResponseEntity<UserTO> register(@RequestBody UserTO user);

    @Operation(summary = "Update user",
            description = "Update user")
    @PutMapping("/users")
    ResponseEntity<Void> user(@RequestBody UserTO user);
}
