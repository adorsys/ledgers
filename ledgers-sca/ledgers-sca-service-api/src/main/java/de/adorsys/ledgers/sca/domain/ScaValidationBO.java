package de.adorsys.ledgers.sca.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScaValidationBO {
    private String authConfirmationCode;
    private boolean validAuthCode;

    public ScaValidationBO(boolean validAuthCode) {
        this.validAuthCode = validAuthCode;
    }
}
