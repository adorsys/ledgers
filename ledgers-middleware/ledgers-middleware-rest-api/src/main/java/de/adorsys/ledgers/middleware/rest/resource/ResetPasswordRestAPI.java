package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;
import de.adorsys.ledgers.security.UpdatePassword;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "LDG008 - Reset password for user")
public interface ResetPasswordRestAPI {
    String BASE_PATH = "/password";

    @PostMapping
    @Operation(summary = "Send code for user password reset")
    ResponseEntity<SendCode> sendCode(@RequestBody ResetPassword resetPassword);

    @PutMapping
    @Operation(summary = "Update user password")
    ResponseEntity<UpdatePassword> updatePassword(@RequestBody ResetPassword resetPassword);
}
