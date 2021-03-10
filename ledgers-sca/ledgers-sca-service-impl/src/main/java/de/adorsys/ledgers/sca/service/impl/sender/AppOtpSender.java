package de.adorsys.ledgers.sca.service.impl.sender;

import de.adorsys.ledgers.sca.domain.sca.message.AppScaMessage;
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

import static de.adorsys.ledgers.um.api.domain.ScaMethodTypeBO.APP_OTP;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppOtpSender implements SCASender<AppScaMessage> {
    private final RestTemplate template;

    @Override
    public boolean send(AppScaMessage message) {
        try {
            HttpMethod method = Optional.ofNullable(HttpMethod.resolve(message.getSocketServiceHttpMethod()))
                                        .orElseThrow(() -> ScaModuleException.buildScaSenderException("Could not parse SocketServiceHttpMethod"));
            HttpEntity<AppScaMessage> httpEntity = new HttpEntity<>(message);
            ResponseEntity<Void> exchange = template.exchange(message.getSocketServicePath(), method, httpEntity, Void.class);
            return exchange.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.error("Could not notify TPP-RestServer through REST");
            return false;
        }
    }

    @Override
    public ScaMethodTypeBO getType() {
        return APP_OTP;
    }
}
