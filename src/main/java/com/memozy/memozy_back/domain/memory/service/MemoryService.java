package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.CreateTempMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.CreateMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.global.dto.PagedResponse;


public interface MemoryService {
    CreateMemoryResponse createMemory(Long userId, CreateMemoryRequest request);
    GetMemoryListResponse getAllByOwnerId(Long userId);
    MemoryDto updateMemory(Long userId, Long memoryId, UpdateMemoryRequest request);
    void deleteMemory(Long memoryId);

    String createTemporaryMemory(Long userId, CreateTempMemoryRequest request);

    GetTempMemoryResponse getTemporaryMemoryItems(String sessionId, Long userId);

    PagedResponse<MemoryInfoDto> searchMyMemories(Long userId, SearchType searchType, String keyword, int page, int size);
}