package com.infinilabs.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("SpringBoot Easysearch")
                        .description("SpringBoot Easysearch API 文档")
                        .version("v1")
                        .license(new License().name("Apache 2.0").url("https://www.infinilabs.com/products/easysearch/")))
                .externalDocs(new ExternalDocumentation()
                        .description("外部文档")
                        .url("https://infinilabs.com/docs/latest/easysearch/"));
    }

}
