package de.adorsys.ledgers.middleware.api.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiddlewareModuleException extends RuntimeException {
    private final MiddlewareErrorCode errorCode;
    private final String devMsg;
}
