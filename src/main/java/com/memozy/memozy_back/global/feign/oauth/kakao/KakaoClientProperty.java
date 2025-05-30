package com.memozy.memozy_back.global.feign.oauth.kakao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("oauth2.client.kakao")
public class KakaoClientProperty {

    private String contentType;
    private String grantType;
    private String clientId;
    private String loginPageUrl;
    private String redirectPath;
}
