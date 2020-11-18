package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.MailScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailOtpMessageHandler implements OtpMessageHandler<MailScaMessage> {
    @Value("${ledgers.sca.authCode.email.body}")
    private String authCodeEmailBody;

    @Value("${ledgers.sca.authCode.email.subject}")
    private String subject;

    @Value("${ledgers.sca.authCode.email.from}")
    private String from;

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.EMAIL;
    }

    @Override
    public MailScaMessage getMessage(AuthCodeDataBO data, ScaUserDataBO scaData, String tan) {
        MailScaMessage message = new MailScaMessage();
        message.setFrom(from);
        message.setTo(scaData.getMethodValue());
        message.setSubject(subject);

        String userMessageTemplate = StringUtils.isBlank(authCodeEmailBody)
                                             ? data.getUserMessage()
                                             : authCodeEmailBody;
        message.setMessage(String.format(userMessageTemplate, tan));
        return message;
    }
}
