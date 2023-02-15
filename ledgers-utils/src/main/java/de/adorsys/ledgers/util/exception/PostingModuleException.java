/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.util.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostingModuleException extends RuntimeException {
    private final PostingErrorCode errorCode;
    private final String devMsg;
}
