package de.adorsys.ledgers.middleware.rest.resource;

import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "LDG013 - Email verification", description = "Provides endpoint for sending mail with verification link and email confirmation.")
public interface ScaVerificationRestAPI {
    String BASE_PATH = "/emails";

    @PostMapping("/email-verification")
    @ApiOperation(value = "Send email for verification", authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Email was successfully send."),
            @ApiResponse(code = 404, message = "Error sending email: verification token or sca data not found.")
    })
    ResponseEntity<Void> sendEmailVerification(@RequestParam("email") String email);

    @GetMapping("/email")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Confirm email")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Email was successfully confirm."),
            @ApiResponse(code = 400, message = "Invalid verification token for email confirmation or email already confirm."),
            @ApiResponse(code = 403, message = "Verification token is expired for email confirmation."),
            @ApiResponse(code = 404, message = "Error confirmation email: sca data not found.")
    })
    ResponseEntity<Void> confirmVerificationToken(@RequestParam("verificationToken") String token);
}