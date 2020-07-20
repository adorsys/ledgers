package de.adorsys.ledgers.app.server;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import de.adorsys.ledgers.app.server.auth.KeycloakConfigProperties;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareResetResource;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.AuthorizationCodeGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SwaggerConfig implements WebMvcConfigurer {
    private static final String OAUTH2 = "apiKey";
    private static final String API_INFO = "api_info.txt";

    private final FileReader fileReader;
    private final BuildProperties buildProperties;
    private final Environment env;
    private final KeycloakConfigProperties keycloakConfigProp;

    @Bean
    public Docket apiDocket() {
        return new Docket(SWAGGER_2)
                       .groupName("001 - LEDGERS API")
                       .apiInfo(apiInfo())
                       .select()
                       .apis(resolvePredicates())
                       .paths(PathSelectors.any())
                       .build()
                       .securitySchemes(singletonList(securitySchema()));
    }

    private Predicate<RequestHandler> resolvePredicates() {
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        return profiles.contains("develop") || profiles.contains("sandbox")
                       ? Predicates.or(RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class), RequestHandlerSelectors.withClassAnnotation(MiddlewareResetResource.class))
                       : RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class);
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                       .clientId(keycloakConfigProp.getResource())
                       .clientSecret(keycloakConfigProp.getCredentials().getSecret())
                       .realm(keycloakConfigProp.getRealm())
                       .appName(keycloakConfigProp.getResource())
                       .scopeSeparator(",")
                       .useBasicAuthenticationWithAccessCodeGrant(false)
                       .build();
    }

    private OAuth securitySchema() {
        GrantType grantType = new AuthorizationCodeGrantBuilder()
                                      .tokenEndpoint(new TokenEndpoint(keycloakConfigProp.getRootPath() + "/protocol/openid-connect/token", "Bearer"))
                                      .tokenRequestEndpoint(new TokenRequestEndpoint(keycloakConfigProp.getRootPath() + "/protocol/openid-connect/auth", keycloakConfigProp.getResource(), keycloakConfigProp.getCredentials().getSecret()))
                                      .build();
        return new OAuthBuilder()
                       .name(OAUTH2)
                       .grantTypes(singletonList(grantType))
                       .scopes(scopes())
                       .build();
    }

    private List<AuthorizationScope> scopes() {
        return singletonList(new AuthorizationScope("global", "accessEverything"));
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("Adorsys GmbH", "https://www.adorsys.de", "fpo@adorsys.de");

        return new ApiInfo(
                "Ledgers", fileReader.getStringFromFile(API_INFO),
                buildProperties.getVersion() + " " + buildProperties.get("build.number"),
                "Terms of Service: to be edited...",
                contact,
                "Apache License Version 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList<>());
    }
}