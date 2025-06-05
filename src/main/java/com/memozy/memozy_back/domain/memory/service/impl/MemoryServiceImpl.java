package com.memozy.memozy_back.domain.memory.service.impl;

import com.memozy.memozy_back.domain.friend.constant.FriendshipStatus;
import com.memozy.memozy_back.domain.friend.repository.FriendshipRepository;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.domain.MemoryShared;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryDto;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.CreateTempMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.CreateMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.repository.MemoryRepository;
import com.memozy.memozy_back.domain.memory.service.MemoryService;
import com.memozy.memozy_back.global.redis.SessionManager;
import com.memozy.memozy_back.global.redis.TemporaryChatStore;
import com.memozy.memozy_back.global.redis.TemporaryMemoryStore;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.domain.file.service.FileService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final FriendshipRepository friendshipRepository;
    private final TemporaryMemoryStore temporaryMemoryStore;
    private final SessionManager sessionManager;
    private final TemporaryChatStore temporaryChatStore;

    @Override
    @Transactional
    public CreateMemoryResponse createMemory(Long ownerId, CreateMemoryRequest request) {
        String sessionId = request.sessionId();
        sessionManager.validateSessionOwner(ownerId, sessionId);

        Memory memory = temporaryMemoryStore.load(sessionId);

        memory.updateBasicInfo(
                request.title(),
                request.category(),
                request.startDate(),
                request.endDate()
        );

        for (MemoryItem item : memory.getMemoryItems()) {
            String movedFileKey = fileService.moveFile(item.getFileKey());
            item.updateFileKey(movedFileKey);
        }

        List<User> newSharedUsers = userRepository.findAllById(request.sharedUsersId());

        memory.getSharedUsers().clear();
        for (User sharedUser : newSharedUsers) {
            // 친구 관계 검증
            checkFriendShip(sharedUser, memory);
            addSharedUser(sharedUser, memory);
        }

        // redis 비우기
        sessionManager.removeSession(sessionId);
        temporaryMemoryStore.remove(sessionId);
        temporaryChatStore.removeSession(sessionId);

        return CreateMemoryResponse.from(
                memoryRepository.save(memory).getId()
        );
    }

    /* 임시 기록 생성(기본 정보 없이) */
    @Override
    public String createTemporaryMemory(Long userId, CreateTempMemoryRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));

        List<TempMemoryItemDto> tempItems = request.memoryItems().stream().map(itemDto -> {
            String rawFileKey = fileService.extractFileKeyFromImageUrl(itemDto.imageUrl());
            String fileKey = fileService.convertHeicIfNeeded(rawFileKey); // HEIC면 JPEG로 변환 후 새 키 리턴
            fileService.validateFileKey(fileKey);
            MemoryItem memoryItem = MemoryItem.createTempMemoryItem(
                    null, // Memory는 임시 객체이므로 null로 시작, 어차피 toDomain에서 다시 할당됨
                    fileKey,
                    "",
                    itemDto.sequence()
            );
            String presignedUrl = fileService.generatePresignedUrlToRead(fileKey).preSignedUrl();
            return TempMemoryItemDto.from(memoryItem, presignedUrl);
        }).toList();

        TempMemoryDto tempDto = new TempMemoryDto(owner.getId(), tempItems);
        String sessionId = sessionManager.createSession(owner.getId());
        temporaryMemoryStore.save(sessionId, tempDto);
        return sessionId;
    }

    @Override
    public GetTempMemoryResponse getTemporaryMemoryItems(String sessionId, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION));
        Memory memory = temporaryMemoryStore.load(sessionId);

        sessionManager.validateSessionOwner(owner.getId(), sessionId);

        return GetTempMemoryResponse.of(memory.getMemoryItems(), fileService);
    }

    @Override
    @Transactional(readOnly = true)
    public GetMemoryListResponse getAllByOwnerId(Long userId) {
        var ownMemories = memoryRepository.findAllByOwnerId(userId);
        var sharedMemories = memoryRepository.findAllSharedByUser(userId);

        var memoryDtoList = Stream.concat(
                ownMemories.stream(), sharedMemories.stream())
                .map(memory -> MemoryDto.from(
                        memory,
                        memory.getMemoryItems().stream()
                                .map(item -> MemoryItemDto.from(
                                        item,
                                        fileService.generatePresignedUrlToRead(item.getFileKey())
                                                .preSignedUrl()
                                )).toList(),
                        fileService)
                )
                .toList();

        return GetMemoryListResponse.from(memoryDtoList);
    }

    @Override
    @Transactional
    public MemoryDto updateMemory(Long userId, Long memoryId, UpdateMemoryRequest request) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));

        if (!memory.getOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_EXCEPTION);
        }

        // 기존 MemoryItem 삭제 후 새로 추가
        memory.getMemoryItems().clear();
        for (MemoryItemDto itemDto : request.memoryItems()) {
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

        var memoryItemDtoList = memory.getMemoryItems().stream()
                .map(item -> MemoryItemDto.from(
                        item,
                        fileService.generatePresignedUrlToRead(item.getFileKey())
                                .preSignedUrl()
                )).toList();

        return MemoryDto.from(memory, memoryItemDtoList, fileService);
    }

    private static void addSharedUser(User user, Memory memory) {
        memory.addSharedUser(MemoryShared.of(memory, user));
    }

    @Override
    @Transactional
    public void deleteMemory(Long memoryId) {
        memoryRepository.deleteById(memoryId);
    }

    private void addMemoryItem(MemoryItemDto item, Memory memory) {
        String fileKey = fileService.extractFileKeyFromImageUrl(item.imageUrl());
        fileService.validateFileKey(fileKey);
        String movedFileKey = fileService.moveFile(fileKey);
        memory.addMemoryItem(
                MemoryItem.create(
                        memory,
                        movedFileKey,
                        item.content(),
                        item.sequence())
        );
    }

    private void checkFriendShip(User sharedUser, Memory memory) {
        boolean isFriend = friendshipRepository.existsFriendshipBetweenUsers(
                memory.getOwner(), sharedUser, FriendshipStatus.ACCEPTED
        );

        if (!isFriend) {
            throw new BusinessException(ErrorCode.FORBIDDEN_FRIEND_ACCESS);
        }
    }
}