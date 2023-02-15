/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EmailVerificationMapperTest {
    private static final String TOKEN = "Fz-4Kb6vREgj38CpsUAtSI";
    private static final EmailVerificationStatus STATUS_PENDING = EmailVerificationStatus.PENDING;
    private static final EmailVerificationStatusBO STATUS_BO_PENDING = EmailVerificationStatusBO.PENDING;
    private static final LocalDateTime EXPIRED_DATE_TIME = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime ISSUED_DATE_TIME = LocalDateTime.now();

    private final EmailVerificationMapper mapper = Mappers.getMapper(EmailVerificationMapper.class);

    @Test
    void toEmailVerificationEntity() {
        // When
        EmailVerificationEntity bo = mapper.toEmailVerificationEntity(buildEmailVerificationBO());

        // Then
        assertEquals(TOKEN, bo.getToken());
        assertEquals(STATUS_PENDING, bo.getStatus());
        assertEquals(EXPIRED_DATE_TIME, bo.getExpiredDateTime());
        assertEquals(ISSUED_DATE_TIME, bo.getIssuedDateTime());
    }

    @Test
    void toEmailVerificationBO() {
        // When
        EmailVerificationBO bo = mapper.toEmailVerificationBO(buildEmailVerificationEntity());

        // Then
        assertEquals(TOKEN, bo.getToken());
        assertEquals(STATUS_BO_PENDING, bo.getStatus());
        assertEquals(EXPIRED_DATE_TIME, bo.getExpiredDateTime());
        assertEquals(ISSUED_DATE_TIME, bo.getIssuedDateTime());
    }

    @Test
    void toEmailVerificationStatus() {
        // When
        EmailVerificationStatus status = mapper.toEmailVerificationStatus(STATUS_BO_PENDING);

        // Then
        assertSame(STATUS_PENDING, status);
    }

    private EmailVerificationEntity buildEmailVerificationEntity() {
        EmailVerificationEntity bo = new EmailVerificationEntity();
        bo.setId(1L);
        bo.setToken(TOKEN);
        bo.setStatus(STATUS_PENDING);
        bo.setExpiredDateTime(EXPIRED_DATE_TIME);
        bo.setIssuedDateTime(ISSUED_DATE_TIME);
        return bo;
    }

    private EmailVerificationBO buildEmailVerificationBO() {
        EmailVerificationBO bo = new EmailVerificationBO();
        bo.setId(1L);
        bo.setToken(TOKEN);
        bo.setStatus(STATUS_BO_PENDING);
        bo.setExpiredDateTime(EXPIRED_DATE_TIME);
        bo.setIssuedDateTime(ISSUED_DATE_TIME);
        return bo;
    }
}