package de.adorsys.ledgers.app.server;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

//import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
//import springfox.documentation.RequestHandler;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//import java.util.function.Predicate;

@Configuration
//@EnableSwagger2
@RequiredArgsConstructor
//@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {
    private static final String API_KEY = "apiKey";
    private static final String API_INFO = "api_info.txt";

    private final FileReader fileReader;
    private final BuildProperties buildProperties;
    private final Environment env;

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
//                       .groupName("001 - LEDGERS API")
//                       .select()
//                       .apis(resolvePredicates())
//                       .paths(PathSelectors.any())
//                       .build()
//                       .pathMapping("/")
//                       .apiInfo(metaData())
//                       .securitySchemes(Collections.singletonList(apiKey()))
//                       .securityContexts(Collections.singletonList(securityContext()));
//
//    }
//
//    private Predicate<RequestHandler> resolvePredicates() {
//        List<String> profiles = Arrays.asList(env.getActiveProfiles());
//        return profiles.contains("develop") || profiles.contains("sandbox")
//                       ? RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class).or(
//                                       RequestHandlerSelectors.withClassAnnotation(MiddlewareResetResource.class))
//                       : RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class);
//    }
//
//    private ApiKey apiKey() {
//        return new ApiKey(API_KEY, "Authorization", "header");
//    }
//
//    private SecurityContext securityContext() {
//        return SecurityContext.builder()
//                       .securityReferences(defaultAuth())
//                       .forPaths(PathSelectors.regex("/*"))
//                       .build();
//    }
//
//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        return Collections.singletonList(new SecurityReference(API_KEY, authorizationScopes));
//    }
//
//    private ApiInfo metaData() {
//        Contact contact = new Contact("Adorsys GmbH", "https://www.adorsys.de", "fpo@adorsys.de");
//
//        return new ApiInfo(
//                "Ledgers", fileReader.getStringFromFile(API_INFO),
//                buildProperties.getVersion() + " " + buildProperties.get("build.number"),
//                "Terms of Service: to be edited...",
//                contact,
//                "Apache License Version 2.0",
//                "https://www.apache.org/licenses/LICENSE-2.0",
//                new ArrayList<>());
//    }
}
