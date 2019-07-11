package de.adorsys.ledgers.deposit.api.service.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.deposit.api.exception.DepositErrorCode.PAYMENT_PROCESSING_FAILURE;

@Service
@RequiredArgsConstructor
public class SerializeService {
    private final ObjectMapper objectMapper;

    public <T> String serializeOprDetails(T orderDetails) {
        try {
            return objectMapper.writeValueAsString(orderDetails);
        } catch (JsonProcessingException e) {
            throw DepositModuleException.builder()
                          .errorCode(PAYMENT_PROCESSING_FAILURE)
                          .devMsg(String.format("Payment object can't be serialized, error message: %s", e.getMessage()))
                          .build();
        }
    }
}
