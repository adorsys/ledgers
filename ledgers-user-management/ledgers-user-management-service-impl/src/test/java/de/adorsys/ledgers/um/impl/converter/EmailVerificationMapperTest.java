package de.adorsys.ledgers.um.impl.converter;

import de.adorsys.ledgers.um.api.domain.EmailVerificationBO;
import de.adorsys.ledgers.um.api.domain.EmailVerificationStatusBO;
import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import org.junit.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class EmailVerificationMapperTest {
    private static final String TOKEN = "Fz-4Kb6vREgj38CpsUAtSI";
    private static final EmailVerificationStatus STATUS_PENDING = EmailVerificationStatus.PENDING;
    private static final EmailVerificationStatusBO STATUS_BO_PENDING = EmailVerificationStatusBO.PENDING;
    private static final LocalDateTime EXPIRED_DATE_TIME = LocalDateTime.now().plusWeeks(1);
    private static final LocalDateTime ISSUED_DATE_TIME = LocalDateTime.now();

    EmailVerificationMapper mapper = Mappers.getMapper(EmailVerificationMapper.class);

    @Test
    public void toEmailVerificationEntity() {
        EmailVerificationEntity bo = mapper.toEmailVerificationEntity(buildEmailVerificationBO());
        assertThat(bo.getToken(), is(TOKEN));
        assertThat(bo.getStatus(), is(STATUS_PENDING));
        assertThat(bo.getExpiredDateTime(), is(EXPIRED_DATE_TIME));
        assertThat(bo.getIssuedDateTime(), is(ISSUED_DATE_TIME));
    }

    @Test
    public void toEmailVerificationBO() {
        EmailVerificationBO bo = mapper.toEmailVerificationBO(buildEmailVerificationEntity());
        assertThat(bo.getToken(), is(TOKEN));
        assertThat(bo.getStatus(), is(STATUS_BO_PENDING));
        assertThat(bo.getExpiredDateTime(), is(EXPIRED_DATE_TIME));
        assertThat(bo.getIssuedDateTime(), is(ISSUED_DATE_TIME));
    }

    @Test
    public void toEmailVerificationStatus() {
        EmailVerificationStatus status = mapper.toEmailVerificationStatus(STATUS_BO_PENDING);

        assertSame(status, STATUS_PENDING);
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