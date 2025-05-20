package com.memozy.memozy_back.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://memozy.shop"))
                .addServersItem(new Server().url("http://localhost:8080"))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")   // 스웨거 Bearer 자동 설정
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name(HttpHeaders.AUTHORIZATION)))
                .info(new Info()
                        .title("MEMOZY API")
                        .description("기록을 쉽게! MEMOZY API 명세서")
                        .version("v1.0.0"));
    }
}