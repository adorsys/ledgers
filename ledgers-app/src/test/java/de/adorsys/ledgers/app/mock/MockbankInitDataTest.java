/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Currency;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MockbankInitDataTest {
    @InjectMocks
    MockbankInitData mockbankInitData = getMockbankInitData();


    @Test
    void testGetUserIdByIban() {
        String result = mockbankInitData.getUserIdByIban("DE32760700240271232100");
        Assertions.assertEquals("accountant", result);
    }


    @Test
    void testGetAccountAccess() {
        Optional<AccountAccessTO> result = mockbankInitData.getAccountAccess("DE32760700240271232100", "g.manager");
        Assertions.assertFalse(result.isEmpty());
        AccountAccessTO accountAccessTO = result.get();
        Assertions.assertEquals(accountAccessTO.getScaWeight(), 90);
        Assertions.assertEquals(accountAccessTO.getCurrency(), Currency.getInstance("EUR"));
    }


    private MockbankInitData getMockbankInitData() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new JavaTimeModule());
        InputStream inputStream = MockBankInitDataConfiguration.class.getResourceAsStream("mockbank-simple-init-data.yml");
        try {
            MockbankInitData mockbankInitData = mapper.readValue(inputStream, MockbankInitData.class);
            mockbankInitData.getUsers()
                    .forEach(u -> u.setId(u.getLogin())); // set userId as login  for test
            return mockbankInitData;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

