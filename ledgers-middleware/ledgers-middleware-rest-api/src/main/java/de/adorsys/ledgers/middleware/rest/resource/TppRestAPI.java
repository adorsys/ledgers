package de.adorsys.ledgers.middleware.rest.resource;


import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "LDG007 - TPP Rest endpoint",
        description= "Provides endpoint for registering, authorizing and managing users for TPPs")

public interface TppRestAPI {

    String BASE_PATH = "/tpps";

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


    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param userCredentials tpp login and tpp pin
     * @return JWT token and user info
     */
    @PostMapping("/login")
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Login",
            notes="Initiates the user login process. Returns a login response object describing how to proceed.")
    @ApiResponses(value={
            @ApiResponse(code=200, response=SCALoginResponseTO.class, message="Success. LoginToken contained in the returned response object."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<SCALoginResponseTO> login(@RequestBody UserTO userCredential) throws NotFoundRestException, ForbiddenRestException;

    /**
     * Creates new user for TPP
     *
     * @param user user object created by TPP
     * @return created user
     */
    @PostMapping("/users")
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Login",
            notes="Create new user for TPP.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<UserTO> createUser(@RequestBody UserTO user) throws UserNotFoundMiddlewareException, ConflictRestException;
}
