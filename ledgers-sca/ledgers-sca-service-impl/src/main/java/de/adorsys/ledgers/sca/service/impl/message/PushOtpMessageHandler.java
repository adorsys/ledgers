package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PushOtpMessageHandler implements OtpMessageHandler {
    @Value("${ledgers.sca.authCode.push.body:http://localhost:8083/tpp/push/tan}")
    private String authCodePushBody;

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.PUSH_OTP;
    }

    @Override
    public String getMessage(AuthCodeDataBO data, String tan) {
        return StringUtils.isBlank(authCodePushBody)
                       ? String.format(data.getUserMessage(), tan)
                       : String.format(authCodePushBody, data.getUserLogin(), data.getOpId(), tan);
    }
}
