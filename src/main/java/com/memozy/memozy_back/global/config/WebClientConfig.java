package com.memozy.memozy_back.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aiWebClient(@Value("${ai.base-url}") String baseUrl, WebClient.Builder builder) {
        return builder.baseUrl(baseUrl).build();
    }
}