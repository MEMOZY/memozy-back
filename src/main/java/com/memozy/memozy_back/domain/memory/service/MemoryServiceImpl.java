package com.memozy.memozy_back.domain.memory.service;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.repository.MemoryRepository;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MemoryInfoDto createMemory(Long ownerId, CreateMemoryRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));

        Memory memory = Memory.create(
                request.title(),
                request.category(),
                request.startDate(),
                request.endDate(),
                owner,
                request.memoryItems(),
                request.sharedUsers()
        );

        return MemoryInfoDto.from(memoryRepository.save(memory));
    }

    @Override
    @Transactional(readOnly = true)
    public GetMemoryListResponse getAllByOwnerId(Long userId) {
        var memoryInfoDtoList = memoryRepository.findAllByUserId(userId).stream()
                .map(MemoryInfoDto::from)
                .toList();

        return GetMemoryListResponse.from(memoryInfoDtoList);
    }

    @Override
    @Transactional
    public MemoryInfoDto updateMemory(Long memoryId, UpdateMemoryRequest request) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));

        memory.update(
                request.title(),
                request.category(),
                request.startDate(),
                request.endDate()
        );

        return MemoryInfoDto.from(memory);
    }

    @Override
    @Transactional
    public void deleteMemory(Long memoryId) {
        memoryRepository.deleteById(memoryId);
    }


    @Override
    @Transactional
    public void addSharedUsers(Long memoryId, Long userId) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));
        memory.addSharedUsers(user);
        memoryRepository.save(memory);
    }
}