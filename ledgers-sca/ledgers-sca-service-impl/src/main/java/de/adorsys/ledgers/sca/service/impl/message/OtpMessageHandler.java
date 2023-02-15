/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;
import de.adorsys.ledgers.sca.service.SCAMethodType;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;

public interface OtpMessageHandler<T extends ScaMessage> extends SCAMethodType {

    T getMessage(AuthCodeDataBO data, ScaUserDataBO scaData, String tan);
}
