package de.adorsys.ledgers.app.server;

import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {


    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                       .groupName("001 - LEDGERS API")
                       .select()
                       .apis(RequestHandlerSelectors.withClassAnnotation(MiddlewareUserResource.class))
                       .paths(PathSelectors.any())
                       .build()
                       .pathMapping("/")
                       .apiInfo(metaData())
                       .securitySchemes(Arrays.asList(apiKey()))
                       .securityContexts(Arrays.asList(securityContext()));

    }

    private ApiKey apiKey() {
        return new ApiKey("apiKey", "Authorization", "header");
    }

    @Bean
    SecurityConfiguration security() {
        return new SecurityConfiguration(null, null, null, // realm Needed for authenticate button to work
                null, // appName Needed for authenticate button to work
                "  ", // apiKeyValue
                ApiKeyVehicle.HEADER, "apiKey", // apiKeyName
                null);
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
        return Arrays.asList(new SecurityReference("apiKey", authorizationScopes));
    }

    private ApiInfo metaData() {

        Contact contact = new Contact("Adorsys GmbH", "https://www.adorsys.de",
                "fpo@adorsys.de");

        return new ApiInfo(
                "Ledgers",
                "Implementation of a simple double entry accounting module with a sample deposit account module."
                        + "<h2>Preloaded Users</h2>"
                        + "<h4>Marion Mueller</h4>"
                        + "<lu>"
                        + "<li>Login: <b>marion.mueller</b></li>"
                        + "<li>PIN: 12345</li>"
                        + "<li>SCA: No SCA Method configured. All operations a retuerned with scaStatus EXEMPTED</li>"
                        + "</lu>"
                        + "<h4>Anton Brueckner</h4>"
                        + "<lu>"
                        + "<li>Login: <b>anton.brueckner</b></li>"
                        + "<li>PIN: 12345</li>"
                        + "<li>SCA: One single sca method configuered."
                        + "<lu>"
                        + "<li>Login SCA: initiated login process will automatically send the TAN to the configuered email and return the scaStatus SCAMETHODSELECTED</li>"
                        + "<li>Payment and Consent SCA: initiated payment or cosent sca will automatically send the TAN to the configuered sca method and return the scaStatus SCAMETHODSELECTED</li>"
                        + "</lu>"
                        + "</li>"
                        + "<li>TAN: configured fake TAN generator will always send the TAN 123456</li>"
                        + "</lu>"
                        + "<h4>Max Musterman</h4>"
                        + "<lu>"
                        + "<li>Login: <b>max.musterman</b></li>"
                        + "<li>PIN: 12345</li>"
                        + "<li>SCA: two sca methods configuered."
                        + "<lu>"
                        + "<li>Login SCA: initiated login process will return the scaStatus PSUIDENTIFIED and a list of sca methods for selection</li>"
                        + "<li>Payment and Consent SCA: initiated payment or cosent sca will return the scaStatus PSUAUTHENTICATED and a list of sca methods for selection</li>"
                        + "</lu>"
                        + "</li>"
                        + "<li>TAN: configured fake TAN generator will always send the TAN 123456</li>"
                        + "</lu>"
                        + "<h2>Registration and Login Process</h2>"
                        + "You can use the User Login Endpoint to gain a login token (or an access token in case of exemption). "
                        + "Then use the access token with the prefix 'Bearer ' to Authorize on this ui before using Select Sca Method and/or Submit Auth Code."
                        + "<h4>Endpoints</h4>"
                        + "<lu>"
                        + "<li>Register: <b>/users/register</b></li>"
                        + "<li>Login: <b>/users/login</b></li>"
                        + "<li>Select Sca Method: <b>/users/{scaId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}</b></li>"
                        + "<li>Submit Auth Code: <b>/users/{scaId}/authorisations/{authorisationId}/authCode</b></li>"
                        + "</lu>"
                        + "<h4>Access Token Types</h4>"
                        + "<lu>"
                        + "<li><b>LOGIN</b>: login token are only issued during the multiphase login process and can not be used to access other resources.</li>"
                        + "<li><b>DIRECT_ACCESS</b>: access token issued to a user after a successfull login (one or two phases). Can be used to access resources</li>"
                        + "<li><b>DELEGATED_ACCESS</b>: delegation token are issued as a result of a successful consent grant or payment authorization. A delegation token issued as result of a succesful payment authorisation can be used to access the corresponding transaction. Delegation token are generaly stored in the consent database and used to authenticate TPP requests with the core banking system.</li>"
                        + "</lu>"
                ,
                "0.5.0",
                "Terms of Service: to be edited...",
                contact,
                "Apache License Version 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList<>());
    }
}
