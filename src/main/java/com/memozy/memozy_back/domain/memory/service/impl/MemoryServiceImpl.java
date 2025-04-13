package com.memozy.memozy_back.domain.memory.service.impl;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.domain.MemoryShared;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.repository.MemoryRepository;
import com.memozy.memozy_back.domain.memory.service.MemoryService;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.util.List;
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

    @Transactional
    public MemoryInfoDto updateMemoryInfo(Long memoryId, UpdateMemoryRequest request) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));

        // 기존 MemoryItem 삭제 후 새로 추가
        memory.getMemoryItems().clear();
        for (MemoryItemDto itemDto : request.memoryItems()) {
            memory.addMemoryItem(MemoryItem.create(
                    itemDto.imageUrl(),
                    itemDto.description(),
                    itemDto.sequence(),
                    memory));
        }

        // sharedUser 변경
        memory.getSharedUsers().clear();
        List<User> newSharedUsers = userRepository.findAllById(request.sharedUserIds());
        for (User user : newSharedUsers) {
            memory.addSharedUser(MemoryShared.of(memory, user));
        }

        // 기본 정보 업데이트
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

}