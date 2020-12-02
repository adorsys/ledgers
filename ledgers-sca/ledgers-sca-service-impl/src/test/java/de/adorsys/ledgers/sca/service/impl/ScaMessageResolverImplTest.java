package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import de.adorsys.ledgers.sca.domain.sca.message.PushScaMessage;
import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;
import de.adorsys.ledgers.sca.service.impl.message.EmailOtpMessageHandler;
import de.adorsys.ledgers.sca.service.impl.message.PushOtpMessageHandler;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.List;

import static de.adorsys.ledgers.sca.service.impl.message.OtpHandlerHelper.MAIL_MSG_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScaMessageResolverImplTest {
    private final EmailOtpMessageHandler handler = new EmailOtpMessageHandler();

    @Test
    void resolveMessage_handler_present() throws NoSuchFieldException {
        PushOtpMessageHandler pushOtpMessageHandler = new PushOtpMessageHandler();
        ScaMessageResolverImpl service = new ScaMessageResolverImpl(List.of(pushOtpMessageHandler), handler);
        FieldSetter.setField(pushOtpMessageHandler,
                             pushOtpMessageHandler.getClass().getDeclaredField("authCodePushBody"), "User: %s initiated an operation : %s requiring TAN confirmation, TAN is: %s");
        ScaMessage result = service.resolveMessage(new AuthCodeDataBO(), new ScaUserDataBO(ScaMethodTypeBO.PUSH_OTP, "POST,http://localhost:8080"), "tan");
        assertNotNull(result);
        assertEquals(PushScaMessage.class, result.getClass());
    }

    @Test
    void resolveMessage_handler_default() throws NoSuchFieldException {
        ScaMessageResolverImpl service = new ScaMessageResolverImpl(List.of(handler), handler);
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("authCodeEmailBody"), MAIL_MSG_PATTERN);
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("subject"), "subj");
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("from"), "from");
        ScaMessage result = service.resolveMessage(new AuthCodeDataBO(), new ScaUserDataBO(ScaMethodTypeBO.PUSH_OTP, "POST,http://localhost:8080"), "tan");
        assertNotNull(result);
        assertEquals(MailScaMessage.class, result.getClass());
    }
}