package com.memozy.memozy_back.global.feign.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoSocialUserProfile {

    private Long id;
    private Properties properties;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    public String getNickname() {
        if (kakaoAccount != null && kakaoAccount.profile != null && kakaoAccount.profile.nickname != null) {
            return kakaoAccount.profile.nickname;
        }
        return properties != null ? properties.nickname : null;
    }

    public String getProfileImageUrl() {
        if (kakaoAccount != null && kakaoAccount.profile != null && kakaoAccount.profile.profileImageUrl != null) {
            return kakaoAccount.profile.profileImageUrl;
        }
        return properties != null ? properties.profileImage : null;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private Profile profile;

        @Getter
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;

            @JsonProperty("profile_image_url")
            private String profileImageUrl;
        }
    }
}