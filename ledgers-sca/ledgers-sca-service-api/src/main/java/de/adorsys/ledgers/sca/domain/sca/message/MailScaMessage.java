package de.adorsys.ledgers.sca.domain.sca.message;

import lombok.Data;

@Data
public class MailScaMessage extends ScaMessage {
    private String from;
    private String to;
    private String subject;
}
