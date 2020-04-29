package de.adorsys.ledgers.sca.service.impl.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender sender;

    @Value("${sca.authCode.email.subject}")
    private String subject;

    @Value("${sca.authCode.email.from}")
    private String from;

    public boolean send(String value, String authCode) {
        log.info("Preparing an email to send auth code");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(value);
            message.setSubject(subject);
            message.setText(authCode);
            message.setFrom(from);
            sender.send(message);
        } catch (MailException e) {
            log.error("Error sending email, No SMTP service configured");
            log.error(e.getMessage());
            return false;
        }
        log.info("Auth code was successfully sent via email");
        return true;
    }

    void setSubject(String subject) {
        this.subject = subject;
    }

    void setFrom(String from) {
        this.from = from;
    }
}
