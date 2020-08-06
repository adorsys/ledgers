package de.adorsys.ledgers.app.server;

import de.adorsys.ledgers.app.server.auth.KeycloakConfigProperties;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
@SecurityScheme(
        type = SecuritySchemeType.OAUTH2,
        scheme = "Bearer",
        name = "Authorization",
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "Bearer",
        flows = @OAuthFlows(authorizationCode = @OAuthFlow(
                authorizationUrl = "${keycloak.auth-server-url}" + "/realms" + "/${keycloak.realm}/" + "protocol/openid-connect/auth",
                tokenUrl = "${keycloak.auth-server-url}" + "/realms" + "/${keycloak.realm}/" + "protocol/openid-connect/token",
                scopes = @OAuthScope(name = "global", description = "accessEverything")
        )))
public class SwaggerConfig implements WebMvcConfigurer {
    private static final String API_KEY = "apiKey";
    private static final String API_INFO = "api_info.txt";

    private final FileReader fileReader;
    private final BuildProperties buildProperties;
    private final Environment env;
    private final KeycloakConfigProperties keycloakConfigProp;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
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

//    @Bean
//    public Docket productApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                       .apis(resolvePredicates())
//                       .build();
//    }
//
//    private Predicate<RequestHandler> resolvePredicates() {
//        List<String> profiles = Arrays.asList(env.getActiveProfiles());
//        return profiles.contains("develop") || profiles.contains("sandbox")
//                       ? RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class).or(
//                                       RequestHandlerSelectors.withClassAnnotation(MiddlewareResetResource.class))
//                       : RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class);
//    }
}