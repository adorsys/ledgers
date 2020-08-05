package de.adorsys.ledgers.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.service.Contact;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
//@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                       .info(new Info().title("Simple ledger")
                                     .contact(new Contact()
                                                      .name("Adorsys GmbH")
                                                      .url("https://www.adorsys.de")
                                                      .email("fpo@adorsys.de"))
                                     .description("Implementation of a simple double entry bookkeeping module.")
                                     .version("0.5.0") //todo set correct version
                                     .termsOfService("Terms of Service: to be edited...")
                                     .license(new License().name("Apache License Version 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
//    @Bean
//    public Docket productApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                       .select()
//                       .apis(RequestHandlerSelectors.any())
//                       .paths(PathSelectors.any())
//                       .build()
//                       .pathMapping("/")
//                       .apiInfo(metaData());
//
//    }
//
//    private ApiInfo metaData() {
//
//        Contact contact = new Contact("Adorsys GmbH", "https://www.adorsys.de",
//                                      "fpo@adorsys.de");
//
//        return new ApiInfo(
//                "Simple ledger",
//                "Implementation of a simple double entry bookkeeping module.",
//                "0.5.0",
//                "Terms of Service: to be edited...",
//                contact,
//                "Apache License Version 2.0",
//                "https://www.apache.org/licenses/LICENSE-2.0",
//                new ArrayList<>());
//    }
//
//    @Override
//    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
}
