package com.memozy.memozy_back.domain.memory.controller;

import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryListResponse;
import com.memozy.memozy_back.domain.memory.service.MemoryService;
import com.memozy.memozy_back.global.jwt.JwtResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/memory")
public class MemoryController {

    private final MemoryService memoryService;

    // 기록 생성
    @PostMapping
    public ResponseEntity<MemoryInfoDto> createMemory(@RequestHeader("X-USER-ID") Long userId,
            @RequestBody CreateMemoryRequest request) {
        return ResponseEntity.ok(memoryService.createMemory(userId, request));
    }

    // 내 기록 전체 조회
    @GetMapping
    public ResponseEntity<GetMemoryListResponse> getMyMemories(@RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(memoryService.getAllByOwnerId(userId));
    }

    // 기록 수정
    @PutMapping("/{memoryId}")
    public ResponseEntity<MemoryInfoDto> updateMemory(@PathVariable Long memoryId,
            @RequestBody UpdateMemoryRequest request) {
        return ResponseEntity.ok(memoryService.updateMemory(memoryId, request));
    }

    // 기록 삭제
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> deleteMemory(@PathVariable Long memoryId) {
        memoryService.deleteMemory(memoryId);
        return ResponseEntity.noContent().build();
    }

    // 기록 공유 유저 추가
    @PostMapping("/{memoryId}/share/{userId}")
    public ResponseEntity<Void> shareMemoryWithUser(@PathVariable Long memoryId,
            @PathVariable Long userId) {
        memoryService.addSharedUsers(memoryId, userId);
        return ResponseEntity.ok().build();
    }
}