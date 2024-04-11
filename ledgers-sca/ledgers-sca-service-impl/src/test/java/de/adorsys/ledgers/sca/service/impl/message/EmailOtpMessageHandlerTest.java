/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static de.adorsys.ledgers.sca.service.impl.message.OtpHandlerHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmailOtpMessageHandlerTest {
    private final EmailOtpMessageHandler handler = new EmailOtpMessageHandler();

    @Test
    void getMessage() {
        ReflectionTestUtils.setField(handler, "authCodeEmailBody", MAIL_MSG_PATTERN);
        ReflectionTestUtils.setField(handler, "subject", "subj");
        ReflectionTestUtils.setField(handler, "from", "from");
        MailScaMessage result = handler.getMessage(getAuthData(), getScaData(ScaMethodTypeBO.SMTP_OTP, true), "TAN");
        assertNotNull(result);
        assertEquals("Your TAN is: TAN", result.getMessage());
        assertEquals("from", result.getFrom());
        assertEquals("subj", result.getSubject());
        assertEquals(EMAIL,result.getTo());
    }
}