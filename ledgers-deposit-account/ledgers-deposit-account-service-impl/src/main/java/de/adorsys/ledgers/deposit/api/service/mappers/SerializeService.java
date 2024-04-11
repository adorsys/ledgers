/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.util.exception.DepositErrorCode.PAYMENT_PROCESSING_FAILURE;

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

    public byte[] serializeObjectToBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException ex) {
            throw DepositModuleException.builder()
                          .errorCode(PAYMENT_PROCESSING_FAILURE)
                          .devMsg(String.format("Object can't be serialized, error message: %s", ex.getMessage()))
                          .build();
        }
    }
}
