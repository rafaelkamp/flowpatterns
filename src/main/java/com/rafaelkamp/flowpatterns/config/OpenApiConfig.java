package com.rafaelkamp.flowpatterns.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flowPatternsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("FlowPatterns API")
                .version("1.0.0")
                .description("API da plataforma de gerenciamento de solicitações e aprovações "
                        + "desenvolvida com Java, Spring Boot e Design Patterns GoF.")
                .contact(new Contact()
                        .name("Rafael Kamp")
                        .url("https://github.com/rafaelkamp"))
                .license(new License().name("MIT")));
    }
}
