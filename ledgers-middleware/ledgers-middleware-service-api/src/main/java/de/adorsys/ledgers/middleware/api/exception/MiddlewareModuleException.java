/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.exception;

import lombok.Builder;
import lombok.Data;

import java.util.function.Supplier;

import static java.lang.String.format;

@Data
@Builder
public class MiddlewareModuleException extends RuntimeException {
    private final MiddlewareErrorCode errorCode;
    private final String devMsg;

    public static Supplier<MiddlewareModuleException> blockedSupplier(MiddlewareErrorCode code, String iban, boolean isRegularBlock) {
        return () -> MiddlewareModuleException.builder()
                             .errorCode(code)
                             .devMsg(format("Operation is Rejected as account: %s is %s", iban, isRegularBlock ? "BLOCKED" : "TEMPORARILY BLOCKED BY SYSTEM"))
                             .build();
    }

    public static MiddlewareModuleException paymentValidationException(String msg) {
        return MiddlewareModuleException.builder()
                       .errorCode(MiddlewareErrorCode.PAYMENT_VALIDATION_EXCEPTION)
                       .devMsg(msg)
                       .build();
    }
}
