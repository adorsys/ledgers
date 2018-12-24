package de.adorsys.ledgers.mockbank.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@ComponentScan(basePackageClasses = MockbankSimpleBasePackage.class)
public class MockBankSimpleConfiguration {
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Bean
    public MockbankInitData init() {

        return loadTestData("mockbank-simple-init-data.yml");
    }


    private MockbankInitData loadTestData(String file) {
        InputStream inputStream = MockbankSimpleBasePackage.class.getResourceAsStream(file);
        try {
            return mapper.readValue(inputStream, MockbankInitData.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}