package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemoryItemDto(
        @NotNull Long id,
        @NotBlank String imageUrl,
        @NotBlank String description,
        @NotNull Integer sequence
) {
    public static MemoryItemDto from(MemoryItem memoryItem) {
        return new MemoryItemDto(
                memoryItem.getId(),
                memoryItem.getImageUrl(),
                memoryItem.getDescription(),
                memoryItem.getSequence()
        );
    }
}
