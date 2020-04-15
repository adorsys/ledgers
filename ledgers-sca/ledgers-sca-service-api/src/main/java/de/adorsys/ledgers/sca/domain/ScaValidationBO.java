package de.adorsys.ledgers.sca.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScaValidationBO {
    private String authConfirmationCode;
    private boolean validAuthCode;
    private ScaStatusBO scaStatus;
    private int attemptsLeft;

    public ScaValidationBO(boolean validAuthCode, int attemptsLeft) {
        this.validAuthCode = validAuthCode;
        this.attemptsLeft = attemptsLeft;
    }
}
