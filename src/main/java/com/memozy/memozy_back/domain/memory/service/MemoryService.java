package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UploadPhotosRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetUploadedPhotoInfoListResponse;

public interface MemoryService {
    MemoryDto createMemory(Long userId, CreateMemoryRequest request);
    GetMemoryListResponse getAllByOwnerId(Long userId);
    MemoryDto updateMemory(Long memoryId, UpdateMemoryRequest request);
    void deleteMemory(Long memoryId);

    GetUploadedPhotoInfoListResponse uploadPhotos(Long userId, UploadPhotosRequest photos);
}