package de.adorsys.ledgers.util.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserManagementModuleException extends RuntimeException {
    private final UserManagementErrorCode errorCode;
    private final String devMsg;
}
