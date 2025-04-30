package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemoryItemDto(
        @NotBlank
        @Schema(
                description = "S3에 업로드된 파일의 키",
                example = "temp/memory/bab.jpeg"
        )
        String fileKey,
        @NotBlank String content,
        @NotNull Integer sequence
) {
    public static MemoryItemDto from(MemoryItem memoryItem) {
        return new MemoryItemDto(
                memoryItem.getFileKey(),
                memoryItem.getContent(),
                memoryItem.getSequence()
        );
    }
}
