package com.memozy.memozy_back.domain.memory.service.impl;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.domain.MemoryShared;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
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
import com.memozy.memozy_back.domain.file.service.FileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Override
    @Transactional
    public MemoryDto createMemory(Long ownerId, CreateMemoryRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));

        Memory memory = Memory.init(
                owner,
                request.title(),
                request.category(),
                request.startDate(),
                request.endDate()
        );

        for (MemoryItemDto itemDto : request.memoryItems()) {
            validateImageUrl(itemDto.imageUrl());
            addMemoryItem(itemDto, memory);
        }

        List<User> newSharedUsers = userRepository.findAllById(request.sharedUsersId());
        for (User sharedUser : newSharedUsers) {
            addSharedUser(sharedUser, memory);
        }

        return MemoryDto.from(memoryRepository.save(memory));
    }

    @Override
    @Transactional(readOnly = true)
    public GetMemoryListResponse getAllByOwnerId(Long userId) {
        var memoryInfoDtoList = memoryRepository.findAllByOwnerId(userId).stream()
                .map(MemoryDto::from)
                .toList();

        return GetMemoryListResponse.from(memoryInfoDtoList);
    }

    @Override
    @Transactional
    public MemoryDto updateMemory(Long memoryId, UpdateMemoryRequest request) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));

        // 기존 MemoryItem 삭제 후 새로 추가
        memory.getMemoryItems().clear();
        for (MemoryItemDto itemDto : request.memoryItems()) {
            validateImageUrl(itemDto.imageUrl());
            addMemoryItem(itemDto, memory);
        }

        // sharedUser 변경
        memory.getSharedUsers().clear();
        List<User> newSharedUsers = userRepository.findAllById(request.sharedUsersId());
        for (User user : newSharedUsers) {
            addSharedUser(user, memory);
        }

        // 기본 정보 업데이트
        memory.update(
                request.title(),
                request.category(),
                request.startDate(),
                request.endDate()
        );

        return MemoryDto.from(memory);
    }

    @Override
    @Transactional
    public void deleteMemory(Long memoryId) {
        memoryRepository.deleteById(memoryId);
    }

    // 파일이 S3에 올라가있는지 검증
    private void validateImageUrl(String imageUrl) {
        if (!fileService.isUploaded(imageUrl)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        }
    }

    private void addMemoryItem(MemoryItemDto item, Memory memory) {
        String imageUrl = fileService.moveFile(item.imageUrl());
        memory.addMemoryItem(
                MemoryItem.create(
                        memory,
                        imageUrl,
                        item.content(),
                        item.sequence())
        );
    }

    private static void addSharedUser(User user, Memory memory) {
        memory.addSharedUser(MemoryShared.of(memory, user));
    }
}