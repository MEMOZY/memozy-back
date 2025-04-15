package com.memozy.memozy_back.global.config;

import com.memozy.memozy_back.global.annotation.V1;
import com.memozy.memozy_back.global.annotation.V2;
import com.memozy.memozy_back.global.http.SocailPlatformRequestConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new SocailPlatformRequestConverter());
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
                .addPathPrefix("/v1", HandlerTypePredicate.forAnnotation(V1.class))
                .addPathPrefix("/v2", HandlerTypePredicate.forAnnotation(V2.class))
                .setPathMatcher(new AntPathMatcher())
                .setUrlPathHelper(new UrlPathHelper())
        ;
    }
}