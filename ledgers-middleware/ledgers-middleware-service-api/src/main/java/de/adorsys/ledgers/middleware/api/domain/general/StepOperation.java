package de.adorsys.ledgers.middleware.api.domain.general;

public enum StepOperation {
    INITIATION,
    START_SCA,
    GET_SCA_METHODS,
    SELECT_SCA_METHOD,
    CONFIRM_AUTH_CODE,
    AUTHORIZE,
    INITIATE_OPERATION_OBJECT
}
