package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;

public record TempMemoryItemDto(
        String tempId,
        MemoryItemDto memoryItem
) {
    public static TempMemoryItemDto of(MemoryItem memoryItem, String presignedUrl) {
        return new TempMemoryItemDto(
                memoryItem.getTempId(),
                MemoryItemDto.from(memoryItem, presignedUrl)
        );
    }
}