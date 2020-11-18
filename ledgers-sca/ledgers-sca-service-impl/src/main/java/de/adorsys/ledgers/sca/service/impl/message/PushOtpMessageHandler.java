package de.adorsys.ledgers.sca.service.impl.message;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.sca.message.PushScaMessage;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PushOtpMessageHandler implements OtpMessageHandler<PushScaMessage> {
    private static final String ERROR_REASON_1_MATCHER = "Could not PUSH TAN for SCA, reason: %s";
    private static final String ERROR_REASON_2_MATCHERS = "Could not PUSH TAN for Sca, reason: %s %s";
    private static final String INVALID_PATTERN_MSG = "Invalid Sca method pattern!\n" +
                                                              "Please update Sca method value to pattern: 'HttpMethod,URI'\n" +
                                                              "sample: 'POST,https://example.com/messenging";
    @Value("${ledgers.sca.authCode.push.body:User: %s initiated an operation : %s requiring TAN confirmation, TAN is: %s}")
    private String authCodePushBody;

    @Override
    public ScaMethodTypeBO getType() {
        return ScaMethodTypeBO.PUSH_OTP;
    }

    @Override
    public PushScaMessage getMessage(AuthCodeDataBO data, ScaUserDataBO scaData, String tan) {
        PushScaMessage message = new PushScaMessage();
        message.setUserLogin(data.getUserLogin());

        //Appropriate format: "PUT,http://localhost:8080/sendit-here"
        List<String> split = getSendProperties(scaData);


        message.setHttpMethod(getHttpMethod(split).name());
        message.setUrl(getUri(split));
        String msg = StringUtils.isBlank(authCodePushBody)
                             ? String.format(data.getUserMessage(), tan)
                             : String.format(authCodePushBody, data.getUserLogin(), data.getOpId(), tan);
        message.setMessage(msg);
        return message;
    }

    private List<String> getSendProperties(ScaUserDataBO scaData) {
        List<String> split = Arrays.stream(scaData.getMethodValue().split(","))
                                     .map(String::trim)
                                     .collect(Collectors.toList());
        if (split.size() != 2) {
            log.error("Malformed PUSH_OTP methodValue: {}, should consist of 2 parts (HttpMethod and URL separated with coma)", scaData.getMethodValue());
            throw ScaModuleException.buildScaSenderException(String.format(ERROR_REASON_1_MATCHER, INVALID_PATTERN_MSG));
        }
        return split;
    }

    private URI getUri(List<String> split) {
        try {
            if (new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS).isValid(split.get(1))) {
                return new URI(split.get(1));
            } else {
                throw new URISyntaxException("", "");
            }
        } catch (URISyntaxException e) {
            throw ScaModuleException.buildScaSenderException(String.format(ERROR_REASON_2_MATCHERS, "Malformed URI ", split.get(1)));
        }
    }

    private HttpMethod getHttpMethod(List<String> split) {
        try {
            return HttpMethod.valueOf(split.get(0));
        } catch (IllegalArgumentException e) {
            throw ScaModuleException.buildScaSenderException(String.format(ERROR_REASON_2_MATCHERS, "Inappropriate HttpMethod", split.get(0)));
        }
    }
}
