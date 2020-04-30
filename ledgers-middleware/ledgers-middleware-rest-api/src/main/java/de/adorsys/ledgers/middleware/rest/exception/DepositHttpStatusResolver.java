package de.adorsys.ledgers.middleware.rest.exception;

import de.adorsys.ledgers.util.exception.DepositErrorCode;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;

import static de.adorsys.ledgers.util.exception.DepositErrorCode.*;
import static org.springframework.http.HttpStatus.*;

public class DepositHttpStatusResolver {
    private static final EnumMap<DepositErrorCode, HttpStatus> container = new EnumMap<>(DepositErrorCode.class);

    private DepositHttpStatusResolver() {
    }

    static {
        //404 Block
        container.put(DEPOSIT_ACCOUNT_NOT_FOUND, NOT_FOUND);
        container.put(PAYMENT_NOT_FOUND, NOT_FOUND);

        //400 Block
        container.put(INSUFFICIENT_FUNDS, BAD_REQUEST);
        container.put(DEPOSIT_ACCOUNT_EXISTS, BAD_REQUEST);
        container.put(PAYMENT_PROCESSING_FAILURE, BAD_REQUEST);
        container.put(PAYMENT_WITH_ID_EXISTS, BAD_REQUEST);
        container.put(DEPOSIT_OPERATION_FAILURE, BAD_REQUEST);
        container.put(CURRENCY_NOT_SUPPORTED,BAD_REQUEST);

        //417 Block
        container.put(COULD_NOT_EXECUTE_STATEMENT, EXPECTATION_FAILED);
    }

    public static HttpStatus resolveHttpStatusByCode(DepositErrorCode code) {
        return container.get(code);
    }
}
