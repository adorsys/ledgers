package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.service.EmailVerificationService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UserVerificationRestAPI.BASE_PATH)
@MiddlewareUserResource
public class UserVerificationResource implements UserVerificationRestAPI {
    private final EmailVerificationService emailVerificationService;

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<Void> sendEmailVerification(String email) {
        String token = emailVerificationService.createVerificationToken(email);
        emailVerificationService.sendVerificationEmail(token);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> confirmVerificationToken(String token) {
        emailVerificationService.confirmUser(token);
        return ResponseEntity.ok().build();
    }
}