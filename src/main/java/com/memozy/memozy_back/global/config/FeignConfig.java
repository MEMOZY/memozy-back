package com.memozy.memozy_back.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.memozy.memozy_back.global")
public class FeignConfig {

}