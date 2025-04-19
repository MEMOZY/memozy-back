package com.memozy.memozy_back.domain.memory.service.impl;

import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.domain.MemoryShared;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.PhotoInfo;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UploadPhotosRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetUploadedPhotoInfoListResponse;
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

        // 사진들이 s3에 올라가있는지 검증
        validatePhotoUrlList(request.memoryItems());

        Memory memory = Memory.create(
                owner,
                request.title(),
                request.category(),
                request.startDate(),
                request.endDate(),
                request.memoryItems(),
                request.sharedUsers()
        );

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
            memory.addMemoryItem(MemoryItem.of(
                            memory,
                            itemDto.imageUrl(),
                            itemDto.content(),
                            itemDto.sequence()
                    )
            );
        }

        // sharedUser 변경
        memory.getSharedUsers().clear();
        List<User> newSharedUsers = userRepository.findAllById(request.sharedUsersId());
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

        return MemoryDto.from(memory);
    }

    @Override
    @Transactional
    public void deleteMemory(Long memoryId) {
        memoryRepository.deleteById(memoryId);
    }

    private void validatePhotoUrlList(List<MemoryItem> memoryItems) {
        for (MemoryItem item : memoryItems) {
            if (!fileService.isUploaded(item.getImageUrl()))
                throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        }
    }
}