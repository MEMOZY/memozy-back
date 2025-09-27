package com.memozy.memozy_back.domain.memory.controller;

import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.dto.request.CreateTempMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.CreateMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.response.CreateTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.service.MemoryService;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.global.annotation.CurrentUserId;
import com.memozy.memozy_back.global.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기록 API", description = "기록 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/memory")
public class MemoryController {

    private final MemoryService memoryService;

    // 기록 생성
    @PostMapping
    public ResponseEntity<CreateMemoryResponse> createMemory(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateMemoryRequest request) {
        return ResponseEntity.ok(memoryService.createMemory(userId, request));
    }

    // 임시 기록 생성(서버 메모리 -> redis)
    @PostMapping("/temp")
    public ResponseEntity<CreateTempMemoryResponse> createTemporaryMemory(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateTempMemoryRequest request
        ) {
        String sessionId = memoryService.createTemporaryMemory(userId, request);
        return ResponseEntity.ok(new CreateTempMemoryResponse(sessionId));
    }

    // 임시 기록 조회(서버 메모리 -> redis)
    @GetMapping("/temp/{sessionId}/items")
    public ResponseEntity<GetTempMemoryResponse> getTemporaryMemoryItems(
            @CurrentUserId Long userId,
            @PathVariable String sessionId) {
        return ResponseEntity.ok(memoryService.getTemporaryMemoryItems(sessionId, userId));
    }


    // 내 기록 및 친구가 공유한 기록 전체 조회
    @GetMapping
    public ResponseEntity<GetMemoryListResponse> getAllMemories(
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(memoryService.getAllByUserId(userId));
    }

    /**
     * 일기 검색
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<MemoryInfoDto>> searchMemories(
            @CurrentUserId Long userId,
            @Parameter(description = "검색 타입", example = "TITLE, CONTENT, ALL")
            @RequestParam(name = "search-type") SearchType searchType,
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(memoryService.searchMyMemories(userId, searchType, keyword, page, size));
    }

    // 기록 수정
    @PutMapping("/{memoryId}")
    public ResponseEntity<MemoryDto> updateMemoryInfo(
            @CurrentUserId Long userId,
            @PathVariable Long memoryId,
            @Valid @RequestBody UpdateMemoryRequest request) {
        return ResponseEntity.ok(memoryService.updateMemory(userId, memoryId, request));
    }

    // 기록 삭제
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> deleteMemory(
            @CurrentUserId Long userId,
            @PathVariable Long memoryId) {
        memoryService.deleteMemory(userId, memoryId);
        return ResponseEntity.noContent().build();
    }

}