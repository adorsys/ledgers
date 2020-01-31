package de.adorsys.ledgers.util.exception;

import lombok.Builder;
import lombok.Data;

import java.util.function.Supplier;

@Data
@Builder
public class UserManagementModuleException extends RuntimeException {
    private final UserManagementErrorCode errorCode;
    private final String devMsg;

    public static Supplier<UserManagementModuleException> getModuleExceptionSupplier(String objectIdentifier, UserManagementErrorCode errorCode, String messagePattern) {
        return () -> UserManagementModuleException.builder()
                             .errorCode(errorCode)
                             .devMsg(String.format(messagePattern, objectIdentifier))
                             .build();
    }
}
