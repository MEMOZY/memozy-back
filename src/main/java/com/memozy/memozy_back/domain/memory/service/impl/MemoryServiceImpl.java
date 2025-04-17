package com.memozy.memozy_back.domain.memory.service.impl;

import com.drew.metadata.Metadata;
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
import com.memozy.memozy_back.domain.file.util.ImageMetadataExtractor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;
    private final FileService s3Uploader;
    private final ImageMetadataExtractor imageMetadataExtractor;

    @Override
    @Transactional
    public MemoryDto createMemory(Long ownerId, CreateMemoryRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));

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

    @Override
    @Transactional
    public GetUploadedPhotoInfoListResponse uploadPhotos(Long userId, UploadPhotosRequest request) {
        List<PhotoInfo> uploaded = new ArrayList<>();

        for (MultipartFile file : request.photos()) {
            // 1. S3 업로드
            String imageUrl = s3Uploader.upload(file, "memory/" + userId);

            // 2. 메타데이터 추출 (촬영일)
            Metadata metadata = imageMetadataExtractor.extract(file);
            LocalDateTime takenAt = imageMetadataExtractor.getTakenDate(metadata);

            uploaded.add(new PhotoInfo(imageUrl, takenAt, -1)); // 시퀀스는 나중에 정렬해서 붙임
        }

        // 3. 촬영일 기준 정렬 및 sequence 부여
        uploaded.sort(Comparator.comparing(PhotoInfo::takenAt, Comparator.nullsLast(LocalDateTime::compareTo)));

        for (int i = 0; i < uploaded.size(); i++) {
            PhotoInfo info = uploaded.get(i);
            uploaded.set(i, new PhotoInfo(info.imageUrl(), info.takenAt(), i + 1));
        }

        return GetUploadedPhotoInfoListResponse.from(uploaded);
    }

}