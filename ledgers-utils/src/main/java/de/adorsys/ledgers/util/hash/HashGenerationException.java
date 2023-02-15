/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.hash;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HashGenerationException extends RuntimeException {

    public HashGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}