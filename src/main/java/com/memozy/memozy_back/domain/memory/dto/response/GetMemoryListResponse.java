package com.memozy.memozy_back.domain.memory.dto.response;

import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import java.util.List;

public record GetMemoryListResponse(
        List<MemoryInfoDto> memories
) {
    public static GetMemoryListResponse from(List<MemoryInfoDto> memoryInfoList) {
        return new GetMemoryListResponse(memoryInfoList);
    }
}
