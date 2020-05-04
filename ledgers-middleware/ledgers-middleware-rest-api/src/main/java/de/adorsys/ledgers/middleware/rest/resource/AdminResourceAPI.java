package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "LDG999 - Admin Management (SYSTEM access)")
public interface AdminResourceAPI {
    String BASE_PATH = "/admin";
    String TPP_ID = "tppId";
    String ROLE = "role";
    String QUERY_PARAM = "queryParam";
    String PAGE = "page";
    String SIZE = "size";
    String PASSWORD = "password";

    @ApiOperation(value = "Get users",
            notes = "Retrieves Page of Users with required role and TPP id",
            authorizations = @Authorization(value = "apiKey"))
    @GetMapping("/users")
    ResponseEntity<CustomPageImpl<UserTO>> users(@RequestParam(value = TPP_ID, defaultValue = "", required = false) String tppId,
                                                 @RequestParam(value = ROLE, required = false) UserRoleTO role,
                                                 @RequestParam(value = QUERY_PARAM, required = false, defaultValue = "") String queryParam,
                                                 @RequestParam(PAGE) int page,
                                                 @RequestParam(SIZE) int size);

    @ApiOperation(value = "Get accounts",
            notes = "Retrieves Page of Accounts with required role and TPP id",
            authorizations = @Authorization(value = "apiKey"))
    @GetMapping("/accounts")
    ResponseEntity<CustomPageImpl<AccountDetailsTO>> accounts(@RequestParam(value = TPP_ID, defaultValue = "", required = false) String tppId,
                                                              @RequestParam(value = QUERY_PARAM, required = false, defaultValue = "") String queryParam,
                                                              @RequestParam(PAGE) int page,
                                                              @RequestParam(SIZE) int size);

    @ApiOperation(value = "Set password for Tpp",
            notes = "Chages password for given TPP",
            authorizations = @Authorization(value = "apiKey"))
    @PutMapping("/password")
    ResponseEntity<Void> updatePassword(@RequestParam(value = TPP_ID) String tppId, @RequestParam(PASSWORD) String password);
}
