package com.memozy.memozy_back.domain.file.dto;

import lombok.Builder;

@Builder
public record PreSignedUrlDto(
        String preSignedUrl,
        String fileKey
) {

}
