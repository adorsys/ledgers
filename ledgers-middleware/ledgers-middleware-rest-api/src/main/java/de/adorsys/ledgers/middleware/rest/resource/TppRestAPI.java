package de.adorsys.ledgers.middleware.rest.resource;


import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "TPP001 - TPP Rest endpoint",
        description= "Provides endpoint for registering, authorizing and managing users for TPPs")

public interface TppRestAPI {

    String BASE_PATH = "/third-party-providers";

    /**
     * Registers a new TPP as a user with technical role.
     * - TPP will be automatically activated.
     *
     * @return
     * @throws ConflictRestException
     */
    @PostMapping("/register")
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Register", notes="Registers a TPP."
            + "<ul>"
            + "<li>A TPP is always registered as a user with technical role. </li>"
            + "<li>A TPP is activated by default. </li>"
            + "</ul>")
    @ApiResponses(value={
            @ApiResponse(code=200, response= UserTO.class, message="The TPP data record without the user pin."),
            @ApiResponse(code=409, message="Conflict. A record with the given email or login already exists.")
    })
    ResponseEntity<UserTO> register(@RequestBody UserTO tpp) throws ConflictRestException;


}
