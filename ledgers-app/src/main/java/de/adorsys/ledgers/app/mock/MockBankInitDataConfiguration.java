package de.adorsys.ledgers.app.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class MockBankInitDataConfiguration {
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Bean
    public MockbankInitData init() {
        mapper.registerModule(new JavaTimeModule());
        return loadTestData("mockbank-simple-init-data.yml");
    }


    private MockbankInitData loadTestData(String file) {
        InputStream inputStream = MockBankInitDataConfiguration.class.getResourceAsStream(file);
        try {
            return mapper.readValue(inputStream, MockbankInitData.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
