/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.server;

import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class SwaggerConfig implements WebMvcConfigurer {
    private static final String API_KEY = "apiKey";
    private static final String API_INFO = "api_info.txt";

    private final FileReader fileReader;
    private final BuildProperties buildProperties;
    private final KeycloakClientConfig keycloakConfigProp;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                       .components(new Components()
                                           .addSecuritySchemes(API_KEY, new SecurityScheme()
                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                .in(SecurityScheme.In.HEADER)
                                                                                .name("Authorization"))
                                           .addSecuritySchemes("oAuth2", new SecurityScheme()
                                                                                 .type(SecurityScheme.Type.OAUTH2)
                                                                                 .in(SecurityScheme.In.HEADER)
                                                                                 .name("Authorization")
                                                                                 .bearerFormat("Bearer")
                                                                                 .flows(new OAuthFlows()
                                                                                                .authorizationCode(new OAuthFlow()
                                                                                                                           .authorizationUrl(keycloakConfigProp.getAuthServerUrl() + "/realms/" + keycloakConfigProp.getClientRealm() + "/protocol/openid-connect/auth")
                                                                                                                           .tokenUrl(keycloakConfigProp.getAuthServerUrl() + "/realms/" + keycloakConfigProp.getClientRealm() + "/protocol/openid-connect/token")
                                                                                                                           .scopes(new Scopes()
                                                                                                                                           .addString("offline_access", "offline_access")
                                                                                                                                           .addString("profile", "profile")
                                                                                                                                           .addString("openid", "openid"))))))


                       .info(new Info().title("Ledgers")
                                     .contact(new Contact()
                                                      .name("Adorsys GmbH")
                                                      .url("https://www.adorsys.de")
                                                      .email("fpo@adorsys.de"))
                                     .description(fileReader.getStringFromFile(API_INFO))
                                     .termsOfService("Terms of Service: to be edited...")
                                     .version(buildProperties.getVersion() + " " + buildProperties.get("build.number"))
                                     .license(new License()
                                                      .name("Apache License Version 2.0")
                                                      .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}