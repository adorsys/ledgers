package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.security.ResetPassword;
import de.adorsys.ledgers.security.SendCode;
import de.adorsys.ledgers.security.UpdatePassword;
import de.adorsys.ledgers.middleware.api.service.MiddlewareResetPasswordService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping(ResetPasswordRestAPI.BASE_PATH)
public class ResetPasswordResources implements ResetPasswordRestAPI {

    private final MiddlewareResetPasswordService middlewarePasswordResetService;

    @Override
    public ResponseEntity<SendCode> sendCode(ResetPassword resetPassword) {
        return ResponseEntity.ok(middlewarePasswordResetService.sendCode(resetPassword));
    }

    @Override
    public ResponseEntity<UpdatePassword> updatePassword(ResetPassword resetPassword) {
        return ResponseEntity.ok(middlewarePasswordResetService.updatePassword(resetPassword));
    }
}
