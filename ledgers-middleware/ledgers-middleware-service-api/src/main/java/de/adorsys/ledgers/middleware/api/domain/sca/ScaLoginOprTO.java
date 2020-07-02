package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.Data;

@Data
public class ScaLoginOprTO {
    private String login;
    private String pin;
    private String oprId;
    private String authorisationId;
    private OpTypeTO opType;
}
