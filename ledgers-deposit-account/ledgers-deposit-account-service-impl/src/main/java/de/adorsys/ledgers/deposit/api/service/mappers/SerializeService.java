package de.adorsys.ledgers.deposit.api.service.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SerializeService {
    private final ObjectMapper objectMapper;

    public <T> String serializeOprDetails(T orderDetails) {
        try {
            return objectMapper.writeValueAsString(orderDetails);
        } catch (JsonProcessingException e) {
            throw new PaymentProcessingException("Payment object can't be serialized", e);
        }
    }
}
