package de.adorsys.ledgers.util.exception;

import lombok.Builder;
import lombok.Data;

import static de.adorsys.ledgers.util.exception.SCAErrorCode.*;

@Data
@Builder
public class ScaModuleException extends RuntimeException {
    private final SCAErrorCode errorCode;
    private final String devMsg;

    public static ScaModuleException buildAttemptsException(int attemptsLeft, boolean isLoginOperation) {
        return attemptsLeft > 0
                       ? resolveAttemptsLeft(attemptsLeft, isLoginOperation)
                       : resolveNoAttemptsLeft(isLoginOperation);
    }

    private static ScaModuleException resolveAttemptsLeft(int attemptsLeft, boolean isLoginOperation) {
        String message = String.format("You have %s attempts to enter valid %s", attemptsLeft, isLoginOperation
                                                                                                       ? "credentials"
                                                                                                       : "TAN");
        SCAErrorCode code = isLoginOperation
                                    ? PSU_AUTH_ATTEMPT_INVALID
                                    : SCA_VALIDATION_ATTEMPT_FAILED;
        return new ScaModuleException(code, message);
    }

    private static ScaModuleException resolveNoAttemptsLeft(boolean isLoginOperation) {
        String message = String.format("Your %s authorization is FAILED please create a new one.", isLoginOperation
                                                                                                           ? "Login"
                                                                                                           : "SCA");
        SCAErrorCode code = isLoginOperation
                                    ? AUTHENTICATION_FAILURE
                                    : SCA_OPERATION_FAILED;
        return new ScaModuleException(code, message);
    }
}
