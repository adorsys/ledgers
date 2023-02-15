/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.ScaMessage;
import de.adorsys.ledgers.sca.service.ScaMessageResolver;
import de.adorsys.ledgers.sca.service.impl.message.EmailOtpMessageHandler;
import de.adorsys.ledgers.sca.service.impl.message.OtpMessageHandler;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScaMessageResolverImpl implements ScaMessageResolver<ScaMessage> {
    private final List<OtpMessageHandler<? extends ScaMessage>> otpMessageHandlers;
    private final EmailOtpMessageHandler defaultOtpMessageHandler;

    @Override
    public ScaMessage resolveMessage(AuthCodeDataBO data, ScaUserDataBO scaData, String tan) {
        return getOtpMessageHandler(scaData.getScaMethod())
                       .getMessage(data, scaData, tan);
    }

    private OtpMessageHandler<? extends ScaMessage> getOtpMessageHandler(ScaMethodTypeBO methodType) {
        return otpMessageHandlers.stream()
                       .filter(h -> h.getType() == methodType)
                       .findFirst()
                       .orElse(defaultOtpMessageHandler);
    }
}
