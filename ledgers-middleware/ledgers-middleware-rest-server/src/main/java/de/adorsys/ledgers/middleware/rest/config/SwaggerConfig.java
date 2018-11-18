package de.adorsys.ledgers.middleware.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                       .select()
                       .apis(RequestHandlerSelectors.any())
                       .paths(PathSelectors.any())
                       .build()
                       .pathMapping("/")
                       .apiInfo(metaData());

    }

    private ApiInfo metaData() {

        Contact contact = new Contact("Adorsys GmbH", "https://www.adorsys.de",
                                      "fpo@adorsys.de");

        return new ApiInfo(
                "Simple ledger",
                "Implementation of a simple double entry bookkeeping module.",
                "0.5.0",
                "Terms of Service: to be edited...",
                contact,
                "Apache License Version 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList<>());
    }
}
