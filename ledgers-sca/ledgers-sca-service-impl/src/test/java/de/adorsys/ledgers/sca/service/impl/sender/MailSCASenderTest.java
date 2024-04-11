/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSCASenderTest {

    private static final String EMAIL = "spe@adorsys.com.ua";
    private static final String FROM = "noreply@adorsys.com.ua";
    private static final String AUTH_CODE = "myAuthCode";
    private static final String SUBJECT = "Email subject";

    @InjectMocks
    private EmailSender sender;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void send() {
        // Given
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        doNothing().when(mailSender).send(captor.capture());
        // When
        boolean result = sender.send(getMailScaMessage());

        SimpleMailMessage message = captor.getValue();

        // Then
        assertThat(result, is(Boolean.TRUE));

        assertThat(message.getTo(), is(Stream.of(EMAIL).toArray()));
        assertThat(message.getFrom(), is(FROM));
        assertThat(message.getText(), is(AUTH_CODE));
        assertThat(message.getSubject(), is(SUBJECT));

        verify(mailSender, times(1)).send(message);
    }

    @NotNull
    private MailScaMessage getMailScaMessage() {
        MailScaMessage scaMessage = new MailScaMessage();
        scaMessage.setSubject(SUBJECT);
        scaMessage.setTo(EMAIL);
        scaMessage.setFrom(FROM);
        scaMessage.setMessage(AUTH_CODE);
        return scaMessage;
    }

    @Test
    void sendNotSend() {
        // Given
        doThrow(MailSendException.class).when(mailSender).send(any(SimpleMailMessage.class));

        // When
        boolean result = sender.send(getMailScaMessage());

        // Then
        assertThat(result, is(Boolean.FALSE));
    }
}