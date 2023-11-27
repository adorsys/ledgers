/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.PushScaMessage;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushOTPSenderTest {

    private static final String DESTINATION_VALUE = "http://localhost:8080/push/endpoint";
    private static final String FROM = "Adorsys Bank";
    private static final String AUTH_CODE = "ThisIsAnAuthCode";
    private static final String SUBJECT = "Your onetime TAN for operation XXX is:";
    private static final String LOGIN = "anton.brueckner";

    @InjectMocks
    private PushOtpSender sender;

    @Mock
    private RestTemplate template;

    @Test
    void send() {
        // Given
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity<String>> bodyCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(template.exchange(any(), any(), any(), eq(Void.class))).thenReturn(ResponseEntity.ok().build());
        // When
        boolean result = sender.send(getPushScaMessage());

        // Then
        assertThat(result, is(Boolean.TRUE));
        verify(template, times(1)).exchange(uriCaptor.capture(), methodCaptor.capture(), bodyCaptor.capture(), eq(Void.class));
        assertEquals("http://localhost:8080/push/endpoint", uriCaptor.getValue().toString());
        assertEquals(HttpMethod.PUT, methodCaptor.getValue());
        assertEquals(SUBJECT, bodyCaptor.getValue().getBody());
    }

    @Test
    void sendFailure() {
        when(template.exchange(any(URI.class), any(), any(), eq(Void.class))).thenThrow(new RestClientException("Wrong!"));
        assertFalse(sender.send(getPushScaMessage()));
    }

    @NotNull
    private PushScaMessage getPushScaMessage() {
        PushScaMessage scaMessage = new PushScaMessage();
        scaMessage.setUserLogin(LOGIN);
        scaMessage.setHttpMethod("PUT");
        scaMessage.setUrl(URI.create(DESTINATION_VALUE));
        scaMessage.setMessage(SUBJECT);
        return scaMessage;
    }
}