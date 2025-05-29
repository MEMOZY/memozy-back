package com.memozy.memozy_back.domain.gpt.dto.response;

import com.memozy.memozy_back.domain.memory.dto.TempMemoryItemDto;
import java.util.List;

public record GetTempMemoryItems(
        List<TempMemoryItemDto> tempMemoryItems
) {

    public static GetTempMemoryItems from(List<TempMemoryItemDto> tempMemoryItemDtos) {
        return new GetTempMemoryItems(tempMemoryItemDtos);
    }
}
