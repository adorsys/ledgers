/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.util.exception.SCAErrorCode.SCA_METHOD_NOT_SUPPORTED;

@Service
public class MobileSCASender implements SCASender<MailScaMessage> {
    @Override
    public boolean send(MailScaMessage message) {
        throw ScaModuleException.builder()
                      .errorCode(SCA_METHOD_NOT_SUPPORTED)
                      .devMsg(String.format("Sending SCA via %s not implemented yet", "PHONE"))
                      .build();
    }

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.MOBILE;
    }
}
