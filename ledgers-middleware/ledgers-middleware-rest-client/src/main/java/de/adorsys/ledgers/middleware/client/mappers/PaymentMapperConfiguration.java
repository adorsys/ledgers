package de.adorsys.ledgers.middleware.client.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentMapperConfiguration {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String DEFAULT_PAYMENT_HOLDER = CLASSPATH_PREFIX + "payment_mapping.yml";
    private static final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final ObjectMapper objectMapper;

    @Value("${payment_mapping.path:}")
    private String paymentMapping;

    @Bean
    public PaymentMapperTO paymentMapperTO() throws IOException {
        try {
            Resource resource = resourceLoader.getResource(resolveYmlToRead());
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            PaymentMapperTO mapperTO = yamlMapper.readValue(resource.getInputStream(), PaymentMapperTO.class);
            mapperTO.setMapper(objectMapper);
            return mapperTO;
        } catch (IOException e) {
            log.error("Could not process payment mapper configuration!");
            throw e;
        }
    }

    private String resolveYmlToRead() {
        return StringUtils.isBlank(paymentMapping)
                       ? DEFAULT_PAYMENT_HOLDER
                       : CLASSPATH_PREFIX + paymentMapping;
    }
}
