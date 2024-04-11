/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.service;

public interface EmailVerificationService {

    /**
     * Create a verification token for email
     *
     * @param email Sca email
     * @return a token
     */
    String createVerificationToken(String email);

    /**
     * Send email with link for email confirmation
     *
     * @param token verification token
     */
    void sendVerificationEmail(String token);

    /**
     * Confirm email
     *
     * @param token verification token
     */
    void confirmUser(String token);
}
