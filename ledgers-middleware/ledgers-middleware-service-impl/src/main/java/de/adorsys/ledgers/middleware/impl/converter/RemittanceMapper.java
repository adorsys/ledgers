/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.middleware.api.domain.payment.RemittanceInformationStructuredTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RemittanceMapper {
    private final ObjectMapper objectMapper;

    public List<RemittanceInformationStructuredTO> mapToRemittanceInformationStructuredTOArray(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, new TypeReference<List<RemittanceInformationStructuredTO>>() {});
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public byte[] mapToRemittanceInformationStructuredTOArray(List<RemittanceInformationStructuredTO> values) {
        if (values == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public List<String> mapToRemittanceInformationUnstructuredArray(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, new TypeReference<List<String>>() {});
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public byte[] mapToRemittanceInformationUnstructuredArray(List<String> values) {
        if (values == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsBytes(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
