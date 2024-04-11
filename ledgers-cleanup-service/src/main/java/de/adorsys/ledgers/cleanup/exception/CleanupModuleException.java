/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.cleanup.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CleanupModuleException extends Exception {
    private final String devMsg;
}
