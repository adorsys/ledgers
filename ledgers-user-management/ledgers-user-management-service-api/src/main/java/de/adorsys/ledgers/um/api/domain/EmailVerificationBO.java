/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain;

import de.adorsys.ledgers.util.Ids;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EmailVerificationBO {

    private Long id;
    private String token;
    private EmailVerificationStatusBO status;
    private LocalDateTime expiredDateTime;
    private LocalDateTime issuedDateTime;
    private LocalDateTime confirmedDateTime;
    private ScaUserDataBO scaUserData;

    public EmailVerificationBO(ScaUserDataBO scaUserData) {
        LocalDateTime now = LocalDateTime.now();
        this.token = Ids.id();
        this.expiredDateTime = now.plusWeeks(1);
        this.status = EmailVerificationStatusBO.PENDING;
        this.issuedDateTime = now;
        this.scaUserData = scaUserData;
    }

    public EmailVerificationBO updateExpiration() {
        LocalDateTime now = LocalDateTime.now();
        expiredDateTime = now.plusWeeks(1);
        issuedDateTime = now;
        return this;
    }

    public void confirmVerification() {
        confirmedDateTime = LocalDateTime.now();
        status = EmailVerificationStatusBO.VERIFIED;
    }

    public boolean isExpired() {
        return getExpiredDateTime().isBefore(LocalDateTime.now());
    }

    public String formatMessage(String message, String basePath, String endpoint, String token, LocalDateTime date) {
        return String.format(message, basePath + endpoint + "?verificationToken=" + token, date.getMonth().toString() + " " + date.getDayOfMonth() + ", " + date.getYear() + " " + date.getHour() + ":" + date.getMinute());
    }
}