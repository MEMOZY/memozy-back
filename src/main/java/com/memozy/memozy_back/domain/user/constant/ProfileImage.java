package com.memozy.memozy_back.domain.user.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileImage {
    DEFAULT("https://memozy-bucket.s3.ap-northeast-2.amazonaws.com/file/profile/default.jpg");

    private final String url;

}
