package de.adorsys.ledgers.middleware.rest.resource;


import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "LDG007 - TPP Rest endpoint",
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


    /**
     * Initiates the user login process. Returns a login response object describing how to proceed.
     *
     * This response object contains an scaId that must be used to proceed with the login.
     *
     * if the {@link SCALoginResponseTO#getScaStatus()} equals
     *   	{@link ScaStatusTO#EXEMPTED} the response will contain the final bearer token.
     * 		{@link ScaStatusTO#SCAMETHODSELECTED} means the auth code has been sent to the user. Must be entered by the user.
     * 	 	{@link ScaStatusTO#PSUAUTHENTICATED} there will be a list of scaMethods for selection in the response.
     * 	 	{@link ScaStatusTO#PSUIDENTIFIED} the user exists but given password/pin did not match.
     *
     * @param login
     * @param pin
     * @param role
     * @return
     * @throws NotFoundRestException
     * @throws ForbiddenRestException : role specified by the user did not match
     */
    @PostMapping("/login")
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Login",
            notes="Initiates the user login process. Returns a login response object describing how to proceed. "
                    + "This response object contains both an scaId and an authorizationId that must be used to identify this login process.<br/>"
                    + "This response also contains an scaStatus that indicates the next stept to take."
                    + "<ul>"
                    + "<li>EXEMPTED: the response will contain the final bearer token."
                    + "<ul>"
                    + "<li>The login process is complete in this single step.</li>"
                    + "<li>The response contains a full JWT access token that can be used to access account and payment endpoints.</li>"
                    + "/ul>"
                    + "</li>"
                    + "<li>SCAMETHODSELECTED: the auth code has been directly sent to the user because the user has only one sca method configured for login. "
                    + "<ul>"
                    + "<li>Auth code Must be entered by the user.</li>"
                    + "<li>Response contains a JWT token that must be used to authenticate for further action. This token can not be used to perform account access because the authentication process is not completed.<li>"
                    + "<li>Caller must proceed with the authCode endpoint: /{scaId}/authorisations/{authorisationId}/authCode</li>"
                    + "</ul>"
                    + "</li>"
                    + "<li>PSUAUTHENTICATED: the user has many sca methods configured for login."
                    + "<ul>"
                    + "<li>The response contains a list of scaMethods for selection</li>. "
                    + "<li>Response contains a JWT token that must be used to authenticate for further calls.</li>"
                    + "<li>This token can not be used to perform account access because the authentication process is not completed.</li>"
                    + "<li>Caller must proceed with the authCode endpoint: /{scaId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}</li>"
                    + "</ul>"
                    + "</li>"
                    + "<li>PSUIDENTIFIED: the user exists but given password/pin did not match.</li>"
                    + "</ul>")
    @ApiResponses(value={
            @ApiResponse(code=200, response=SCALoginResponseTO.class, message="Success. LoginToken contained in the returned response object."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<SCALoginResponseTO> login(@RequestBody UserTO userCredential) throws NotFoundRestException, ForbiddenRestException;



}
