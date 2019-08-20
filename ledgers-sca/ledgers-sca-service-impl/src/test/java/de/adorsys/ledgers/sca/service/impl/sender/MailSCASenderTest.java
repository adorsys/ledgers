package de.adorsys.ledgers.sca.service.impl.sender;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MailSCASenderTest {

    private static final String EMAIL = "spe@adorsys.com.ua";
    private static final String FROM = "noreply@adorsys.com.ua";
    private static final String AUTH_CODE = "myAuthCode";
    private static final String SUBJECT = "Email subject";

    @InjectMocks
    private EmailSender sender;

    @Mock
    private JavaMailSender mailSender;

    @Before
    public void setUp() {
        sender.setSubject(SUBJECT);
        sender.setFrom(FROM);
    }

    @Test
    public void send() {

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        doNothing().when(mailSender).send(captor.capture());

        boolean result = sender.send(EMAIL, AUTH_CODE);

        SimpleMailMessage message = captor.getValue();

        assertThat(result, is(Boolean.TRUE));

        assertThat(message.getTo(), is(Stream.of(EMAIL).toArray()));
        assertThat(message.getFrom(), is(FROM));
        assertThat(message.getText(), is(AUTH_CODE));
        assertThat(message.getSubject(), is(SUBJECT));

        verify(mailSender, times(1)).send(message);
    }

    @Test
    public void sendNotSend() {

        doThrow(MailSendException.class).when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = sender.send(EMAIL, AUTH_CODE);

        assertThat(result, is(Boolean.FALSE));
    }
}