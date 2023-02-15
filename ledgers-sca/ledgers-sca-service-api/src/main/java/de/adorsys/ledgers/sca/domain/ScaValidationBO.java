/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

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

    public ScaValidationBO(boolean validAuthCode) {
        this.validAuthCode = validAuthCode;
    }
}
