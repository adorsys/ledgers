/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;

public class OtpHandlerHelper {
    public static final String LOGIN = "login";
    public static final String SCA_ID = "scaDataId";
    public static final String OP_ID = "opId";
    public static final String EX_ID = "externalId";
    public static final String MSG = "msg";
    public static final String AUTH_ID = "authId";
    public static final String EMAIL = "email@test.de";
    public static final String PUSH_VALUE = "PUT,http://localhost:8080/sendit-here";
    public static final String APP_URL = "http://localhost:8090/api/v1/decoupled/message";
    public static final String PUSH_MSG_PATTERN = "User: %s initiated an operation : %s requiring TAN confirmation, TAN is: %s";
    public static final String APP_MSG_PATTERN = "Do you confirm your %s id: %s";
    public static final String MAIL_MSG_PATTERN = "Your TAN is: %s";

    public static AuthCodeDataBO getAuthData() {
        return new AuthCodeDataBO(LOGIN, SCA_ID, OP_ID, EX_ID, MSG, 100, OpTypeBO.PAYMENT, AUTH_ID, 100);
    }

    public static ScaUserDataBO getScaData(ScaMethodTypeBO methodType, boolean correct) {
        return new ScaUserDataBO(SCA_ID, methodType, correct && methodType == ScaMethodTypeBO.PUSH_OTP ? PUSH_VALUE : EMAIL, true, "123456", true);
    }
}
