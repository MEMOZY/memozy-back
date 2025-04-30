package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;

public record TempMemoryItemDto(
        String tempId,
        MemoryItemDto memoryItem
) {
    public static TempMemoryItemDto from(MemoryItem memoryItem) {
        return new TempMemoryItemDto(
                String.valueOf(memoryItem.getTempId()),
                MemoryItemDto.from(memoryItem)
        );
    }
}