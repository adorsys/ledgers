package de.adorsys.ledgers.util.exception;

import lombok.Builder;
import lombok.Data;

import java.util.function.Supplier;

import static java.lang.String.format;

@Data
@Builder
public class UserManagementModuleException extends RuntimeException {
    private final UserManagementErrorCode errorCode;
    private final String devMsg;

    public static Supplier<UserManagementModuleException> getModuleExceptionSupplier(String objectIdentifier, UserManagementErrorCode errorCode, String messagePattern) {
        return () -> UserManagementModuleException.builder()
                             .errorCode(errorCode)
                             .devMsg(format(messagePattern, objectIdentifier))
                             .build();
    }

    public static Supplier<UserManagementModuleException> getUserBlockedSupplier(boolean isRegularBlock) {
        return () -> UserManagementModuleException.builder()
                             .errorCode(isRegularBlock
                                                ? UserManagementErrorCode.USER_IS_BLOCKED
                                                : UserManagementErrorCode.USER_IS_TEMPORARY_BLOCKED_BY_SYSTEM)
                             .devMsg(format("Operation is rejected as user is %s", isRegularBlock
                                                                                           ? "BLOCKED"
                                                                                           : "TEMPORARILY BLOCKED BY SYSTEM"))
                             .build();
    }

}
