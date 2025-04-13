package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.user.domain.User;
import java.util.List;

public interface MemoryService {
    MemoryInfoDto createMemory(Long userId, CreateMemoryRequest request);
    GetMemoryListResponse getAllByOwnerId(Long userId);
    MemoryInfoDto updateMemory(Long memoryId, UpdateMemoryRequest request);
    void deleteMemory(Long memoryId);

    void addSharedUsers(Long memoryId, Long userId);
}