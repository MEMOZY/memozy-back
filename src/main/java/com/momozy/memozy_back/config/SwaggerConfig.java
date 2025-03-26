package com.momozy.memozy_back.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MEMOZY API")
                        .description("기록을 쉽게! MEMOZY API 명세서")
                        .version("v1.0.0")
                );
    }
}