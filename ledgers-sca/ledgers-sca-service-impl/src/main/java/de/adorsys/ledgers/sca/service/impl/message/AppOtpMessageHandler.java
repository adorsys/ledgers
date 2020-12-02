package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.AppScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppOtpMessageHandler implements OtpMessageHandler<AppScaMessage> {
    @Value("${ledgers.sca.authCode.app_otp.template: Do you confirm your %s id: %s }")
    private String messageTemplate;
    @Value("${ledgers.sca.authCode.app_otp.socket_service.httpMethod: POST}")
    private String socketServiceHttpMethod;
    @Value("${ledgers.sca.authCode.app_otp.socket_service.url: http://localhost:8090/api/v1/decoupled/message}")
    private String socketServiceUrl;

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.APP_OTP;
    }

    @Override
    public AppScaMessage getMessage(AuthCodeDataBO data, ScaUserDataBO scaData, String tan) {
        AppScaMessage msg = new AppScaMessage();

        msg.setMessage(String.format(messageTemplate, data.getOpType().name(), data.getOpId()));
        msg.setAddressedUser(data.getUserLogin());
        msg.setAuthorizationId(data.getAuthorisationId());
        msg.setAuthorizationTTL(data.getValiditySeconds());
        msg.setConfirmationUrl(null);//Shall be filled by OBA
        msg.setHttpMethod(null);//Shall be filled by OBA
        msg.setObjId(data.getOpId());
        msg.setOpType(data.getOpType());
        msg.setAuthCode(tan);
        msg.setSocketServiceHttpMethod(socketServiceHttpMethod);
        msg.setSocketServicePath(socketServiceUrl);
        return msg;
    }
}
