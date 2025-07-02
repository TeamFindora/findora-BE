package com.findora.findora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Findora API")
                        .description("Findora 프로젝트 API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Findora Team")
                                .email("contact@findora.com")));
    }
}