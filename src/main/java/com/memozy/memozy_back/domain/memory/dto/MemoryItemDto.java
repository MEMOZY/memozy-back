package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemoryItemDto(
        @NotNull Long id,
        @NotBlank
        @Schema(
                description = "S3에 업로드된 파일의 url",
                example = "https://bucket.s3.ap-northeast-2.amazonaws.com/file/memory/abc.jpg"
        )
        String imageUrl,
        @NotBlank String content,
        @NotNull Integer sequence
) {
    public static MemoryItemDto from(MemoryItem memoryItem) {
        return new MemoryItemDto(
                memoryItem.getId(),
                memoryItem.getImageUrl(),
                memoryItem.getContent(),
                memoryItem.getSequence()
        );
    }
}
