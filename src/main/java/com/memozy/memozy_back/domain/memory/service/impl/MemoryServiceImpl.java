package com.memozy.memozy_back.domain.memory.service.impl;

import com.memozy.memozy_back.domain.friend.constant.FriendshipStatus;
import com.memozy.memozy_back.domain.friend.repository.FriendshipRepository;
import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryAccess;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.dto.MemoryAccessDto;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryDto;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.request.AccessGrantRequest;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.CreateTempMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.CreateMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.repository.MemoryAccessRepository;
import com.memozy.memozy_back.domain.memory.repository.MemoryItemRepository;
import com.memozy.memozy_back.domain.memory.repository.MemoryRepository;
import com.memozy.memozy_back.domain.memory.service.MemoryService;
import com.memozy.memozy_back.global.dto.PagedResponse;
import com.memozy.memozy_back.global.redis.SessionManager;
import com.memozy.memozy_back.global.redis.TemporaryChatStore;
import com.memozy.memozy_back.global.redis.TemporaryMemoryStore;
import com.memozy.memozy_back.domain.user.domain.User;
import com.memozy.memozy_back.domain.user.repository.UserRepository;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import com.memozy.memozy_back.domain.file.service.FileService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MemoryServiceImpl implements MemoryService {

    private final MemoryRepository memoryRepository;
    private final MemoryItemRepository memoryItemRepository;
    private final MemoryAccessRepository memoryAccessRepository;
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

        List<AccessGrantRequest> grants = request.accesses() == null
                ? List.of() : request.accesses();

        List<MemoryAccess> newAccesses = getMemoryAccesses(ownerId, grants, memory);

        memory.clearAndSetAccesses(newAccesses);

        // redis 비우기
        sessionManager.removeSession(sessionId);
        temporaryMemoryStore.remove(sessionId);
        temporaryChatStore.removeSession(sessionId);

        return CreateMemoryResponse.from(
                memoryRepository.save(memory).getId()
        );
    }

    @Override
    @Transactional
    public MemoryDto updateMemory(Long userId, Long memoryId, UpdateMemoryRequest request) {
        Memory memory = memoryRepository.findByIdWithAccessesAndItems(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));

        if (!canEditContent(memory, userId)) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_EXCEPTION);
        }

        memory.getMemoryItems().clear();
        for (MemoryItemDto itemDto : request.memoryItems()) {
            addMemoryItem(itemDto, memory);
        }

        if (request.accesses() != null && !request.accesses().isEmpty()) {
            if (!canManageAccesses(memory, userId)) {
                throw new BusinessException(ErrorCode.INVALID_PERMISSION_LEVEL);
            }

            List<AccessGrantRequest> grants = request.accesses();
            List<MemoryAccess> newAccesses = getMemoryAccesses(userId, grants, memory);
            memory.clearAndSetAccesses(newAccesses);
        }

        memory.update(
                request.title(),
                request.category(),
                request.startDate(),
                request.endDate()
        );

        var memoryItemDtoList = memory.getMemoryItems().stream()
                .map(item -> MemoryItemDto.from(
                        item,
                        fileService.generatePresignedUrlToRead(item.getFileKey()).preSignedUrl()
                )).toList();

        var accessDtoList = memory.getAccesses().stream()
                .map(MemoryAccessDto::of)
                .toList();

        return MemoryDto.from(memory, memoryItemDtoList, accessDtoList);
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
    public PagedResponse<MemoryInfoDto> searchMyMemories(Long userId, SearchType searchType, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size); // 정렬은 쿼리에서 처리
        if (searchType == null) {
            throw new BusinessException(ErrorCode.MISSING_SEARCH_TYPE);
        }
        Page<Memory> paged = memoryRepository.searchByKeyword(userId, searchType, keyword, pageable);

        if (paged.isEmpty()) {
            return new PagedResponse<>(
                    List.of(),
                    paged.getNumber(),
                    paged.getSize(),
                    paged.getTotalElements(),
                    paged.getTotalPages(),
                    paged.isLast()
            );
        }

        // 대표 아이템 한 번에 조회
        List<Long> memoryIds = paged.getContent().stream().map(Memory::getId).toList();
        Map<Long, MemoryItem> firstItems = memoryItemRepository.findFirstItemsByMemoryIds(memoryIds);

        List<MemoryInfoDto> items = paged.getContent().stream()
                .map(memory -> {
                    MemoryItem first = firstItems.get(memory.getId());
                    String content = (first != null) ? first.getContent() : null;
                    String thumbnailUrl = null;
                    if (first != null) {
                        String fileKey = first.getFileKey();
                        if (fileKey != null && !fileKey.isBlank()) {
                            thumbnailUrl = fileService.generatePresignedUrlToRead(fileKey).preSignedUrl();
                        }
                    }

                    PermissionLevel permissionLevel = findPermissionLevel(memory, userId);

                    boolean canEditContent = canEditContent(userId, memory, permissionLevel);

                    return new MemoryInfoDto(
                            memory.getId(),
                            memory.getOwner().getId(),
                            memory.getTitle(),
                            content,
                            memory.getStartDate(),
                            memory.getEndDate(),
                            thumbnailUrl,
                            permissionLevel,
                            canEditContent
                    );
                })
                .toList();

        return new PagedResponse<>(
                items,
                paged.getNumber(),
                paged.getSize(),
                paged.getTotalElements(),
                paged.getTotalPages(),
                paged.isLast()
        );
    }



    @Override
    @Transactional(readOnly = true)
    public GetMemoryListResponse getAllByUserId(Long userId) {
        List<Memory> ownMemories = memoryRepository.findAllByOwnerIdWithItems(userId);
        List<MemoryAccess> accesses = memoryAccessRepository.findAllByUserIdWithMemoryAndItems(userId);

        List<Memory> sharedMemories = accesses.stream()
                .map(MemoryAccess::getMemory)
                .toList();

        // 3) DTO 변환
        Stream<Memory> allMyMemories = Stream.concat(ownMemories.stream(), sharedMemories.stream());

        List<MemoryInfoDto> dtoList = allMyMemories
                .map(memory -> {
                    String thumbnailUrl = memory.getMemoryItems().stream()
                            .findFirst()
                            .map(item -> fileService.generatePresignedUrlToRead(item.getFileKey()).preSignedUrl())
                            .orElse(null);

                    String content = memory.getMemoryItems().stream()
                            .findFirst()
                            .map(MemoryItem::getContent)
                            .orElse(null);

                    PermissionLevel permissionLevel = findPermissionLevel(memory, userId);
                    boolean canEdit = canEditContent(userId, memory, permissionLevel);

                    return new MemoryInfoDto(
                            memory.getId(),
                            memory.getOwner().getId(),
                            memory.getTitle(),
                            content,
                            memory.getStartDate(),
                            memory.getEndDate(),
                            thumbnailUrl,
                            permissionLevel,
                            canEdit
                    );
                })
                .toList();

        return GetMemoryListResponse.from(dtoList);
    }


    @Override
    @Transactional
    public void deleteMemory(Long userId, Long memoryId) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION));
        if (!memory.getOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.INVALID_ACCESS_EXCEPTION);
        }
        memoryRepository.deleteById(memoryId);
    }

    private void addMemoryItem(MemoryItemDto item, Memory memory) {
        String fileKey = fileService.extractFileKeyFromImageUrl(item.imageUrl());
        fileService.validateFileKey(fileKey);
        String movedFileKey = fileService.moveFile(
                fileService.convertHeicIfNeeded(fileKey)
        );
        memory.addMemoryItem(
                MemoryItem.create(
                        memory,
                        movedFileKey,
                        item.content(),
                        item.sequence())
        );
    }


    private boolean canEditContent(Memory memory, Long userId) {
        if (memory.getOwner().getId().equals(userId)) return true;
        return memory.getAccesses().stream()
                .anyMatch(a -> a.getUser().getId().equals(userId)
                        && (a.getPermissionLevel() == PermissionLevel.EDITOR));
    }

    private static boolean canEditContent(Long userId, Memory memory, PermissionLevel permissionLevel) {
        return memory.getOwner().getId().equals(userId) ||
                permissionLevel == PermissionLevel.EDITOR || permissionLevel == PermissionLevel.OWNER;
    }

    private boolean canManageAccesses(Memory m, Long userId) {
        return m.getOwner().getId().equals(userId);
    }

    private List<MemoryAccess> getMemoryAccesses(Long ownerId, List<AccessGrantRequest> grants, Memory memory) {
        // userId 목록 추출해서 한 번에 조회
        List<Long> userIds = grants.stream().map(AccessGrantRequest::userId).toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 중복/오너 포함 체크
        Set<Long> seen = new HashSet<>();
        List<MemoryAccess> newAccesses = new ArrayList<>();

        for (AccessGrantRequest g : grants) {
            Long targetUserId = g.userId();

            if (targetUserId.equals(ownerId)) {
                throw new BusinessException(ErrorCode.CANNOT_MANAGE_OWNER_ACCESS);
            }

            if (!seen.add(targetUserId)) {
                continue;
            }

            User target = userMap.get(targetUserId);
            if (target == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_USER_EXCEPTION);
            }

            checkFriendShip(target, memory);

            newAccesses.add(MemoryAccess.create(memory, target, g.permissionLevel()));
        }
        return newAccesses;
    }

    private void checkFriendShip(User sharedUser, Memory memory) {
        boolean isFriend = friendshipRepository.existsFriendshipBetweenUsers(
                memory.getOwner(), sharedUser, FriendshipStatus.ACCEPTED
        );

        if (!isFriend) {
            throw new BusinessException(ErrorCode.FORBIDDEN_FRIEND_ACCESS);
        }
    }

    private PermissionLevel findPermissionLevel(Memory memory, Long userId) {
        return memory.getAccesses().stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .map(MemoryAccess::getPermissionLevel)
                .findFirst()
                .orElse(PermissionLevel.OWNER);
    }
}