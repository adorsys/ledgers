package de.adorsys.ledgers.middleware.rest.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                       .components(new Components()
                                           .addSecuritySchemes("apiKey", new SecurityScheme()
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
                                                                                                                           .authorizationUrl("someUrl" + "/realms/" + "someUrl" + "/protocol/openid-connect/auth")
                                                                                                                           .tokenUrl("someUrl" + "/realms/" + "someUrl" + "/protocol/openid-connect/token")
                                                                                                                           .scopes(new Scopes()
                                                                                                                                           .addString("offline_access", "offline_access")
                                                                                                                                           .addString("profile", "profile")
                                                                                                                                           .addString("openid", "openid"))))))


                       .info(new Info().title("Ledgers")
                                     .contact(new Contact()
                                                      .name("Adorsys GmbH")
                                                      .url("https://www.adorsys.de")
                                                      .email("fpo@adorsys.de"))
                                     .description("someUrl")
                                     .termsOfService("Terms of Service: to be edited...")
                                     .license(new License()
                                                      .name("Apache License Version 2.0")
                                                      .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
