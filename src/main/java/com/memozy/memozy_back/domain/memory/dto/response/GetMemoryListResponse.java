package com.memozy.memozy_back.domain.memory.dto.response;

import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import java.util.List;

public record GetMemoryListResponse(
        List<MemoryDto> memories
) {
    public static GetMemoryListResponse from(List<MemoryDto> memoryInfoList) {
        return new GetMemoryListResponse(memoryInfoList);
    }
}
