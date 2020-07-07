package de.adorsys.ledgers.middleware.impl.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Getter
@Configuration
@ConfigurationProperties(prefix = "ledgers.currency")
public class CurrencyConfigurationProperties {
    private final Set<Currency> currencies = new HashSet<>();
}
