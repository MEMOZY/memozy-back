package com.memozy.memozy_back.domain.memory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemoryItemDto(
        @NotNull Long id,
        @NotBlank String imageUrl,
        @NotBlank String content,
        @NotNull Integer sequence
) {
}
