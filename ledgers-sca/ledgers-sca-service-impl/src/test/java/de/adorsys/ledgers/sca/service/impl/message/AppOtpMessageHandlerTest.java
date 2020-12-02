package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.sca.message.AppScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.PAYMENT;
import static de.adorsys.ledgers.sca.service.impl.message.OtpHandlerHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppOtpMessageHandlerTest {
    private final AppOtpMessageHandler handler = new AppOtpMessageHandler();

    @Test
    void getMessage() throws NoSuchFieldException {
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("messageTemplate"), APP_MSG_PATTERN);
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("socketServiceHttpMethod"), "POST");
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("socketServiceUrl"), APP_URL);

        AppScaMessage result = handler.getMessage(OtpHandlerHelper.getAuthData(), OtpHandlerHelper.getScaData(ScaMethodTypeBO.APP_OTP, true), "TAN");
        assertNotNull(result);
        assertEquals(OP_ID, result.getObjId());
        assertEquals(PAYMENT, result.getOpType());
        assertEquals(AUTH_ID, result.getAuthorizationId());
        assertEquals(100, result.getAuthorizationTTL());
        assertEquals(LOGIN, result.getAddressedUser());
        assertEquals("TAN", result.getAuthCode());
        assertEquals("POST", result.getSocketServiceHttpMethod());
        assertEquals(APP_URL, result.getSocketServicePath());
        assertEquals("Do you confirm your PAYMENT id: opId", result.getMessage());
    }
}