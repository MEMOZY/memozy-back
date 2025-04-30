package com.memozy.memozy_back.domain.memory.dto.response;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryItemDto;
import java.util.List;

public record GetTempMemoryResponse(
        List<TempMemoryItemDto> memoryItems
) {
    public static GetTempMemoryResponse from(List<MemoryItem> memoryItemList) {
        List<TempMemoryItemDto> items = memoryItemList.stream()
                .map(TempMemoryItemDto::from)
                .toList();
        return new GetTempMemoryResponse(items);
    }
}