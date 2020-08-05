package de.adorsys.ledgers.middleware.rest.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "LDG009 - Email verification", description = "Provides endpoint for sending mail with verification link and email confirmation.")
public interface ScaVerificationRestAPI {
    String BASE_PATH = "/emails";

    @PostMapping("/email-verification")
    @Operation(summary = "Send email for verification")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email was successfully send."),
            @ApiResponse(responseCode = "404", description = "Error sending email: verification token or sca data not found.")
    })
    ResponseEntity<Void> sendEmailVerification(@RequestParam("email") String email);

    @GetMapping("/email")
    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, summary = "Confirm email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email was successfully confirm."),
            @ApiResponse(responseCode = "400", description = "Invalid verification token for email confirmation or email already confirm."),
            @ApiResponse(responseCode = "403", description = "Verification token is expired for email confirmation."),
            @ApiResponse(responseCode = "404", description = "Error confirmation email: sca data not found.")
    })
    ResponseEntity<Void> confirmVerificationToken(@RequestParam("verificationToken") String token);
}