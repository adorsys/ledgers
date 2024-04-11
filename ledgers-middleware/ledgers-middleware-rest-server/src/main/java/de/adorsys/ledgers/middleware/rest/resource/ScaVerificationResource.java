/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.service.EmailVerificationService;
import de.adorsys.ledgers.middleware.impl.config.EmailVerificationProperties;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ScaVerificationRestAPI.BASE_PATH)
@MiddlewareUserResource
public class ScaVerificationResource implements ScaVerificationRestAPI {
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationProperties verificationProperties;

    @Override
    public ResponseEntity<Void> sendEmailVerification(String email) {
        String token = emailVerificationService.createVerificationToken(email);
        emailVerificationService.sendVerificationEmail(token);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> confirmVerificationToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        String location = verificationProperties.getPage().getSuccess();
        try {
            emailVerificationService.confirmUser(token);
        } catch (Exception e) {
            location = verificationProperties.getPage().getFail();
        }
        headers.add(HttpHeaders.LOCATION, location);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}