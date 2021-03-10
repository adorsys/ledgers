package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.PushScaMessage;
import de.adorsys.ledgers.sca.service.SCASender;
import de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.PUSH_OTP;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushOtpSender implements SCASender<PushScaMessage> {
    private final RestTemplate template;

    @Override
    public boolean send(PushScaMessage message) {

        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(message.getMessage());
            HttpMethod httpMethod = Optional.ofNullable(HttpMethod.resolve(message.getHttpMethod()))
                                            .orElseThrow(() -> ScaModuleException.buildScaSenderException("Could not resolve HttpMethod for PsuhOTP Sender " + message.getHttpMethod()));
            ResponseEntity<Void> exchange = template.exchange(message.getUrl(), httpMethod, httpEntity, Void.class);
            return exchange.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Could not deliver PUSH_OTP message, reason: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ScaMethodTypeBO getType() {
        return PUSH_OTP;
    }
}
