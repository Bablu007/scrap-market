package com.scrapmarket.scrap_market.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Scrap Management API")
                        .version("1.0.0")
                        .description("API documentation for managing products and sellers")
                        .contact(new Contact()
                                .name("Bablu Agrahari")
                                .email("babluagrahari07@gmail.com")
                        )
                );
    }
}
