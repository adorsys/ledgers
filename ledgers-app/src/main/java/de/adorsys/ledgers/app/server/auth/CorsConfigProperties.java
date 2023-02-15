/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.server.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "management.endpoints.web.cors")
public class CorsConfigProperties {
    private Boolean allowCredentials;
    private List<String> allowedOriginPatterns;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private long maxAge;
}
