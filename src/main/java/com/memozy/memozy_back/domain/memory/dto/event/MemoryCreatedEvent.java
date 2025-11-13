package com.memozy.memozy_back.domain.memory.dto.event;

public record MemoryCreatedEvent(
        Long memoryId,
        Long ownerId,
        String sessionId
) {

}
