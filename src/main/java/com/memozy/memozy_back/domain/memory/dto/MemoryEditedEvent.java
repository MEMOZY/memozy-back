package com.memozy.memozy_back.domain.memory.dto;

public record MemoryEditedEvent(
    Long memoryId,
    Long userId,
    String token
) {
}
