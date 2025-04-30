package com.memozy.memozy_back.domain.memory.controller;

import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateTempMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.CreateTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.service.MemoryService;
import com.memozy.memozy_back.global.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기록 API", description = "기록 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/memory")
public class MemoryController {

    private final MemoryService memoryService;

    // 기록 생성
    @PostMapping
    public ResponseEntity<MemoryDto> createMemory(
            @CurrentUserId Long userId,
            @RequestBody CreateMemoryRequest request) {
        return ResponseEntity.ok(memoryService.createMemory(userId, request));
    }

    // 임시 기록 생성(서버 메모리 -> redis)
    @PostMapping("/temp")
    public ResponseEntity<CreateTempMemoryResponse> createTemporaryMemory(
            @RequestBody CreateTempMemoryRequest request,
            @CurrentUserId Long userId) {
        String sessionId = memoryService.createTemporaryMemory(userId, request);
        return ResponseEntity.ok(new CreateTempMemoryResponse(sessionId));
    }

    // 임시 기록 조회(서버 메모리 -> redis)
    @GetMapping("/temp/{sessionId}/items")
    public ResponseEntity<GetTempMemoryResponse> getTemporaryMemoryItems(
            @PathVariable String sessionId,
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(memoryService.getTemporaryMemoryItems(sessionId, userId));
    }


    // 내 기록 전체 조회
    @GetMapping
    public ResponseEntity<GetMemoryListResponse> getMyMemories(
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(memoryService.getAllByOwnerId(userId));
    }

    // 기록 수정
    @PutMapping("/{memoryId}")
    public ResponseEntity<MemoryDto> updateMemoryInfo(
            @PathVariable Long memoryId,
            @RequestBody UpdateMemoryRequest request) {
        return ResponseEntity.ok(memoryService.updateMemory(memoryId, request));
    }

    // 기록 삭제
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> deleteMemory(@PathVariable Long memoryId) {
        memoryService.deleteMemory(memoryId);
        return ResponseEntity.noContent().build();
    }

}