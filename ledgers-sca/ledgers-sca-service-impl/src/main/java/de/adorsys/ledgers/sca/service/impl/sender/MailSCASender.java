/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailSCASender implements SCASender {
    private final JavaMailSender sender;

    @Value("${sca.authCode.email.subject}")
    private String subject;

    @Value("${sca.authCode.email.from}")
    private String from;

    @Override
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
            return false;
        }
        log.info("Auth code was successfully sent via email");
        return true;
    }

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.EMAIL;
    }

    void setSubject(String subject) {
        this.subject = subject;
    }

    void setFrom(String from) {
        this.from = from;
    }
}
