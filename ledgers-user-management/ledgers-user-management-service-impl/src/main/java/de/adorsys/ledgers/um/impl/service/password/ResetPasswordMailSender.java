package de.adorsys.ledgers.um.impl.service.password;

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
public class ResetPasswordMailSender {
    @Value("${reset-password.email.subject}")
    private String subject;
    @Value("${reset-password.email.from}")
    private String from;

    private final JavaMailSender sender;

    public boolean send(String recipient, String text) {
        log.info("Preparing an email to send forget password code");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(from);
            sender.send(message);
        } catch (MailException e) {
            log.error("Error sending email, No SMTP service configured");
            return false;
        }
        log.info("Code was successfully sent via email");
        return true;
    }
}