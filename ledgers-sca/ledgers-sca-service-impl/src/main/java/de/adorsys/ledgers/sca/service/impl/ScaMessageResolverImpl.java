package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.service.ScaMessageResolver;
import de.adorsys.ledgers.sca.service.impl.message.EmailOtpMessageHandler;
import de.adorsys.ledgers.sca.service.impl.message.OtpMessageHandler;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScaMessageResolverImpl implements ScaMessageResolver {
    private final List<OtpMessageHandler> otpMessageHandlers;
    private final EmailOtpMessageHandler defaultOtpMessageHandler;

    @Override
    public String resolveMessage(AuthCodeDataBO data, String tan, ScaMethodTypeBO methodType) {
        return getOtpMessageHandler(methodType)
                       .getMessage(data, tan);
    }

    private OtpMessageHandler getOtpMessageHandler(ScaMethodTypeBO methodType) {
        return otpMessageHandlers.stream()
                       .filter(h -> h.getType() == methodType)
                       .findFirst()
                       .orElse(defaultOtpMessageHandler);
    }
}
