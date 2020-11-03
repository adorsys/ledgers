package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailOtpMessageHandler implements OtpMessageHandler {
    @Value("${ledgers.sca.authCode.email.body}")
    private String authCodeEmailBody;

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.EMAIL;
    }

    @Override
    public String getMessage(AuthCodeDataBO data, String tan) {
        String userMessageTemplate = StringUtils.isBlank(authCodeEmailBody)
                                             ? data.getUserMessage()
                                             : authCodeEmailBody;
        return String.format(userMessageTemplate, tan);
    }
}
