/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.sca.message.AppScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.PAYMENT;
import static de.adorsys.ledgers.sca.service.impl.message.OtpHandlerHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppOtpMessageHandlerTest {
    private final AppOtpMessageHandler handler = new AppOtpMessageHandler();

    @Test
    void getMessage() {
        ReflectionTestUtils.setField(handler, "messageTemplate", APP_MSG_PATTERN);
        ReflectionTestUtils.setField(handler, "socketServiceHttpMethod", "POST");
        ReflectionTestUtils.setField(handler, "socketServiceUrl", APP_URL);

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