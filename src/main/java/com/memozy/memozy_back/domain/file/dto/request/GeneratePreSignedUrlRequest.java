package com.memozy.memozy_back.domain.file.dto.request;

import com.memozy.memozy_back.domain.file.constant.FileDomain;
import jakarta.validation.constraints.NotNull;

public record GeneratePreSignedUrlRequest(
        @NotNull String fileName,
        @NotNull FileDomain fileDomain
) {
}
