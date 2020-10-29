package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.internal.util.reflection.FieldSetter.setField;

@ExtendWith(MockitoExtension.class)
class PushOTPSenderTest {

    private static final String DESTINATION_VALUE = "PUT,http://localhost:8080/push/endpoint";
    private static final String DESTINATION_VALUE_INVALID_SEPARATION = "PUT: http://localhost:8080/push/endpoint";
    private static final String DESTINATION_VALUE_INVALID_VALUE = "PUT";
    private static final String DESTINATION_VALUE_WRONG_METHOD = "PUD,http://localhost:8080/push/endpoint";
    private static final String DESTINATION_VALUE_INVALID_URI = "PUT,http://localhost:qaaaa/push/endpoint";
    private static final String FROM = "Adorsys Bank";
    private static final String AUTH_CODE = "ThisIsAnAuthCode";
    private static final String SUBJECT = "Your onetime TAN for operation XXX is:";

    @InjectMocks
    private PushOtpSender sender;

    @Mock
    private RestTemplate template;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        setField(sender, sender.getClass().getDeclaredField("subject"), SUBJECT);
        setField(sender, sender.getClass().getDeclaredField("from"), FROM);
    }

    @Test
    void send() {
        // Given
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity<String>> bodyCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(template.exchange(any(), any(), any(), eq(Void.class))).thenReturn(ResponseEntity.ok().build());

        // When
        boolean result = sender.send(DESTINATION_VALUE, AUTH_CODE);

        // Then
        assertThat(result, is(Boolean.TRUE));
        verify(template, times(1)).exchange(uriCaptor.capture(), methodCaptor.capture(), bodyCaptor.capture(), eq(Void.class));
        assertEquals("http://localhost:8080/push/endpoint", uriCaptor.getValue().toString());
        assertEquals(HttpMethod.PUT, methodCaptor.getValue());
        assertEquals(String.format("from: %s, %s %s", FROM, SUBJECT, AUTH_CODE), bodyCaptor.getValue().getBody());
    }

    @Test
    void invalidMethod() {
        // When
        assertThrows(ScaModuleException.class, () -> sender.send(DESTINATION_VALUE_WRONG_METHOD, AUTH_CODE));

        // Then
        verify(template, times(0)).exchange(any(), any(), any(), eq(Void.class));
    }

    @Test
    void invalidDestinationComposition() {
        // When
        assertThrows(ScaModuleException.class, () -> sender.send(DESTINATION_VALUE_INVALID_SEPARATION, AUTH_CODE));

        // Then
        verify(template, times(0)).exchange(any(), any(), any(), eq(Void.class));
    }

    @Test
    void invalidDestinationComposition2() {
        // When
        assertThrows(ScaModuleException.class, () -> sender.send(DESTINATION_VALUE_INVALID_VALUE, AUTH_CODE));

        // Then
        verify(template, times(0)).exchange(any(), any(), any(), eq(Void.class));
    }

    @Test
    void invalidUri() {
        // When
        assertThrows(ScaModuleException.class, () -> sender.send(DESTINATION_VALUE_INVALID_URI, AUTH_CODE));

        // Then
        verify(template, times(0)).exchange(any(), any(), any(), eq(Void.class));
    }
}