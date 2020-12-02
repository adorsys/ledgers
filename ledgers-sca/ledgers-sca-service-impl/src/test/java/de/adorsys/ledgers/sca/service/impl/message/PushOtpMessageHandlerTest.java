package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.PushScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;

import java.net.URI;
import java.net.URISyntaxException;

import static de.adorsys.ledgers.sca.service.impl.message.OtpHandlerHelper.*;
import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.PUSH_OTP;
import static de.adorsys.ledgers.util.exception.SCAErrorCode.SCA_SENDER_ERROR;
import static org.junit.jupiter.api.Assertions.*;

class PushOtpMessageHandlerTest {
    private final PushOtpMessageHandler handler = new PushOtpMessageHandler();

    @Test
    void getMessage() throws NoSuchFieldException, URISyntaxException {
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("authCodePushBody"), PUSH_MSG_PATTERN);
        PushScaMessage result = handler.getMessage(getAuthData(), getScaData(PUSH_OTP, true), "TAN");
        assertNotNull(result);
        assertEquals("User: login initiated an operation : opId requiring TAN confirmation, TAN is: TAN", result.getMessage());
        assertEquals(LOGIN, result.getUserLogin());
        assertEquals(PUSH_VALUE.split(",")[0], result.getHttpMethod());
        assertEquals(new URI(PUSH_VALUE.split(",")[1]), result.getUrl());
    }

    @Test
    void getMessage_wrong_method_value() throws NoSuchFieldException {
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("authCodePushBody"), PUSH_MSG_PATTERN);
        AuthCodeDataBO authData = getAuthData();
        ScaUserDataBO scaData = getScaData(PUSH_OTP, false);
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> handler.getMessage(authData, scaData, "TAN"));
        assertEquals(SCA_SENDER_ERROR, exception.getErrorCode());
        assertTrue(exception.getDevMsg().contains("Invalid Sca method pattern!"));
    }

    @Test
    void getMessage_invalid_uri_in_method_value() throws NoSuchFieldException {
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("authCodePushBody"), PUSH_MSG_PATTERN);
        AuthCodeDataBO authData = getAuthData();
        ScaUserDataBO scaData = getScaData(PUSH_OTP, false);
        scaData.setMethodValue("POST,htttP:///ssssss.com");
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> handler.getMessage(authData, scaData, "TAN"));
        assertEquals(SCA_SENDER_ERROR, exception.getErrorCode());
        assertTrue(exception.getDevMsg().contains("Malformed URI"));
    }

    @Test
    void getMessage_invalid_http_method_in_method_value() throws NoSuchFieldException {
        FieldSetter.setField(handler, handler.getClass().getDeclaredField("authCodePushBody"), PUSH_MSG_PATTERN);
        AuthCodeDataBO authData = getAuthData();
        ScaUserDataBO scaData = getScaData(PUSH_OTP, false);
        scaData.setMethodValue("POZT,http://localhost:8088");
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> handler.getMessage(authData, scaData, "TAN"));
        assertEquals(SCA_SENDER_ERROR, exception.getErrorCode());
        assertTrue(exception.getDevMsg().contains("Inappropriate HttpMethod"));
    }
}