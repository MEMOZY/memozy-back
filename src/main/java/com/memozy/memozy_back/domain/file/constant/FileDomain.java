package com.memozy.memozy_back.domain.file.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileDomain {
    PROFILE_DEFAULT_IMAGE("/profile/default", false),
    PROFILE_IMAGE("/profile", false),
    MEMORY_PHOTOS("/", false),
    CHAT("/chat", true);

    private final String directory;
    private final boolean isTemporary;
}

