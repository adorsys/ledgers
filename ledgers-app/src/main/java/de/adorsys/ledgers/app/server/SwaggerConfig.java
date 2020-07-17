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
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SwaggerConfig implements WebMvcConfigurer {
    private static final String REST_CONTROLLER_PACKAGE = "de.beplus.user.controller";
    private static final String OAUTH2 = "apiKey";

    private final KeycloakConfigProperties keycloakConfigProp;
    private final Environment env;

    @Bean
    public Docket apiDocket() {
        return new Docket(SWAGGER_2)
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
        return singletonList(new AuthorizationScope("profile", "Access profile API"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                       .title("API - User service")
                       .contact(new Contact("Be+", "https://beplus.de", "info@beplus.de"))
                       .description("User service API")
                       .license("Apache License Version 2.0")
                       .version("1.0")
                       .build();
    }

}
/*@Configuration
@EnableSwagger2
@RequiredArgsConstructor
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {
    private static final String API_KEY = "apiKey";
    private static final String API_INFO = "api_info.txt";

    private final FileReader fileReader;
    private final BuildProperties buildProperties;
    private final Environment env;

    @Bean
    public Docket productApi() {
        return new Docket(SWAGGER_2)
                       .groupName("001 - LEDGERS API")
                       .select()
                       .apis(resolvePredicates())
                       .paths(PathSelectors.any())
                       .build()
                       .pathMapping("/")
                       .apiInfo(metaData())
                       .securitySchemes(singletonList(apiKey()))
                       .securityContexts(singletonList(securityContext()));

    }

    private Predicate<RequestHandler> resolvePredicates() {
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        return profiles.contains("develop") || profiles.contains("sandbox")
                       ? (RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class)
                                  .or(RequestHandlerSelectors.withClassAnnotation(MiddlewareResetResource.class)))
                       : RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class);
    }

    private ApiKey apiKey() {
        return new ApiKey(API_KEY, "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                       .securityReferences(defaultAuth())
                       .forPaths(PathSelectors.regex("/*"))
                       .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return singletonList(new SecurityReference(API_KEY, authorizationScopes));
    }

    private ApiInfo metaData() {
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
}*/
