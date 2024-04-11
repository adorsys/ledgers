/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositModuleException extends RuntimeException {
    private final DepositErrorCode errorCode;
    private final String devMsg;
}
