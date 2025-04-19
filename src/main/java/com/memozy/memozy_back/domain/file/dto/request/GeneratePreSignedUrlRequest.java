package com.memozy.memozy_back.domain.file.dto.request;

import com.memozy.memozy_back.domain.file.constant.FileDomain;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record GeneratePreSignedUrlRequest(
        @NotNull
        @Schema(
                description = "S3에 저장할 파일명",
                example = "82371adf-90a4-478b-a73d-photo.jpg"
        )
        String fileName,

        @NotNull
        @Schema(
                description = "파일 도메인 (ex: PROFILE_DEFAULT_IMAGE, PROFILE_IMAGE, MEMORY_TEMP_PHOTOS, MEMORY_PHOTOS)",
                example = "MEMORY_PHOTOS"
        )
        FileDomain fileDomain
) {
}
