package com.memozy.memozy_back.domain.memory.dto.response;

import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import java.util.List;

public record GetTempMemoryResponse(
        List<MemoryItemDto> memoryItems
) {
    public static GetTempMemoryResponse from(List<MemoryItemDto> memoryItemList) {
        return new GetTempMemoryResponse(memoryItemList);
    }
}
