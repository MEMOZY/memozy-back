package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemoryItemDto(
        @NotBlank
        @Schema(
                description = "S3에 업로드된 파일 URL",
                example = "https://memozy.s3.ap-northeast-2.amazonaws.com/temp/memozy/1234567890.jpg"
        )
        String imageUrl,
        @Nullable String content,
        @NotNull Integer sequence
) {
    public static MemoryItemDto from(MemoryItem memoryItem, String presignedUrl) {
        return new MemoryItemDto(
                presignedUrl,
                memoryItem.getContent(),
                memoryItem.getSequence()
        );
    }
}
