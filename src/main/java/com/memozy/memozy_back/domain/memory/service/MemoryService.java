package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;

public interface MemoryService {
    MemoryInfoDto createMemory(Long userId, CreateMemoryRequest request);
    GetMemoryListResponse getAllByOwnerId(Long userId);
    MemoryInfoDto updateMemoryInfo(Long memoryId, UpdateMemoryRequest request);
    void deleteMemory(Long memoryId);
}