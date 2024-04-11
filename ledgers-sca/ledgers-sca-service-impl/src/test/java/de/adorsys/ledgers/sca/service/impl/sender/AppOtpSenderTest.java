/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.AppScaMessage;
import de.adorsys.ledgers.sca.service.impl.message.OtpHandlerHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppOtpSenderTest {
    @InjectMocks
    private AppOtpSender sender;
    @Mock
    private RestTemplate restTemplate;

    @Test
    void send() {
        when(restTemplate.exchange(eq(OtpHandlerHelper.APP_URL), eq(HttpMethod.POST), any(), eq(Void.class))).thenReturn(ResponseEntity.ok().build());
        boolean result = sender.send(getMsg());
        assertTrue(result);
    }

    @Test
    void send_error() {
        when(restTemplate.exchange(eq(OtpHandlerHelper.APP_URL), eq(HttpMethod.POST), any(), eq(Void.class))).thenThrow(RestClientException.class);
        boolean result = sender.send(getMsg());
        assertFalse(result);
    }

    private AppScaMessage getMsg() {
        AppScaMessage message = new AppScaMessage();
        message.setSocketServiceHttpMethod("POST");
        message.setSocketServicePath(OtpHandlerHelper.APP_URL);
        return message;
    }

}