package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.PUSH_OTP;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushOtpSender implements SCASender {
    private static final String MESSAGE_PATTERN = "from: %s, %s %s";
    private static final String ERROR_REASON_2_MATCHERS = "Could not PUSH TAN for Sca, reason: %s %s";
    private static final String ERROR_REASON_1_MATCHER = "Could not PUSH TAN for SCA, reason: %s";
    private static final String INVALID_PATTERN_MSG = "Invalid Sca method pattern!\n" +
                                                              "Please update Sca method value to pattern: 'HttpMethod,URI'\n" +
                                                              "sample: 'POST,https://example.com/messenging";
    private final RestTemplate template;

    @Value("${ledgers.sca.authCode.email.subject}")
    private String subject;

    @Value("${ledgers.sca.authCode.email.from}")
    private String from;

    @Override
    /*
    Appropriate format: "PUT,http://localhost:8080/sendit-here"
     */
    public boolean send(String value, String authCode) {
        List<String> split = Arrays.stream(value.split(","))
                                     .map(String::trim)
                                     .collect(Collectors.toList());
        if (split.size() != 2) {
            log.error("Malformed PUSH_OTP methodValue: {}, should consist of 2 parts (HttpMethod and URL separated with coma)",value);
            throw ScaModuleException.buildScaSenderException(String.format(ERROR_REASON_1_MATCHER, INVALID_PATTERN_MSG));
        }
        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(String.format(MESSAGE_PATTERN, from, subject, authCode));
            ResponseEntity<Void> exchange = template.exchange(getUri(split), getHttpMethod(split), httpEntity, Void.class);
            return exchange.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Could not deliver PUSH_OTP message, reason: {}",e.getMessage());
            throw ScaModuleException.buildScaSenderException(String.format(ERROR_REASON_2_MATCHERS, e.getMessage(), "\nWe will try to re-send the message later."));
        }
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

    @Override
    public ScaMethodTypeBO getType() {
        return PUSH_OTP;
    }
}
