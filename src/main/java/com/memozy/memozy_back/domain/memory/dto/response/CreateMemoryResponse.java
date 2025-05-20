package com.memozy.memozy_back.domain.memory.dto.response;

public record CreateMemoryResponse(
        Long memoryId
) {
    public static CreateMemoryResponse from(Long memoryId) {
        return new CreateMemoryResponse(memoryId);
    }
}
