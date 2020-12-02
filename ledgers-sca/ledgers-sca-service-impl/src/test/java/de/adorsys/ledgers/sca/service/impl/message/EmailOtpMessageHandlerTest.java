package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import static de.adorsys.ledgers.sca.service.impl.message.OtpHandlerHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmailOtpMessageHandlerTest {
    private final EmailOtpMessageHandler handler = new EmailOtpMessageHandler();

    @Test
    void getMessage() throws NoSuchFieldException {
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("authCodeEmailBody"), MAIL_MSG_PATTERN);
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("subject"), "subj");
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("from"), "from");
        MailScaMessage result = handler.getMessage(getAuthData(), getScaData(ScaMethodTypeBO.EMAIL, true), "TAN");
        assertNotNull(result);
        assertEquals("Your TAN is: TAN", result.getMessage());
        assertEquals("from", result.getFrom());
        assertEquals("subj", result.getSubject());
        assertEquals(EMAIL,result.getTo());
    }
}