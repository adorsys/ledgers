/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthCodeDataConverterTest {

    @Test
    void toAuthCodeDataBO() {
        // Given
        AuthCodeDataConverter mapper = Mappers.getMapper(AuthCodeDataConverter.class);

        AuthCodeDataTO to = readYml();

        // When
        AuthCodeDataBO bo = mapper.toAuthCodeDataBO(to);

        // Then
        assertEquals(to.getOpId(), bo.getOpId());
        assertEquals(to.getUserLogin(), bo.getUserLogin());
        assertEquals(to.getScaUserDataId(), bo.getScaUserDataId());
    }

    private <T> T readYml() {
        try {
            return YamlReader.getInstance().getObjectFromInputStream(getClass().getResourceAsStream("auth-code-data.yml"), (Class<T>) AuthCodeDataTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}