package de.adorsys.ledgers.um.impl.service.password;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMailSender {

    private final JavaMailSender sender;

    public boolean send(String subject, String from, String to, String text) {
        log.info("Preparing an email to send code");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(from);
            sender.send(message);
        } catch (MailException e) {
            log.error("Error sending email, No SMTP service configured");
            log.error(e.getMessage());
            log.error(e.getStackTrace().toString());
            return false;
        }
        log.info("Code was successfully sent via email");
        return true;
    }
}