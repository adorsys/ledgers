/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender sender;

    public boolean send(MailScaMessage scaMessage) {
        log.info("Preparing an email to send auth code");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(scaMessage.getTo());
            message.setFrom(scaMessage.getFrom());
            message.setSubject(scaMessage.getSubject());
            message.setText(scaMessage.getMessage());
            sender.send(message);
        } catch (MailException e) {
            log.error("Error sending email, No SMTP service configured");
            log.error(e.getMessage());
            return false;
        }
        log.info("Auth code was successfully sent via email");
        return true;
    }
}
