package com.memozy.memozy_back.domain.memory.controller;

import com.memozy.memozy_back.domain.memory.constant.MemoryCategory;
import com.memozy.memozy_back.domain.memory.constant.SearchType;
import com.memozy.memozy_back.domain.memory.dto.CalendarFilter;
import com.memozy.memozy_back.domain.memory.dto.request.CreateEditLockResponse;
import com.memozy.memozy_back.domain.memory.dto.request.CreateTempMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.DeleteEditLockRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateEditLockRequest;
import com.memozy.memozy_back.domain.memory.dto.response.CreateMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.response.CreateTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;
import com.memozy.memozy_back.domain.memory.dto.request.CreateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.request.UpdateMemoryRequest;
import com.memozy.memozy_back.domain.memory.dto.response.GetMemoryDetailsResponse;
import com.memozy.memozy_back.domain.memory.dto.response.GetTempMemoryResponse;
import com.memozy.memozy_back.domain.memory.dto.response.UpdateEditLockTTLResponse;
import com.memozy.memozy_back.domain.memory.service.MemoryEditLockService;
import com.memozy.memozy_back.domain.memory.service.MemoryService;
import com.memozy.memozy_back.domain.memory.dto.MemoryInfoDto;
import com.memozy.memozy_back.global.annotation.CurrentUserId;
import com.memozy.memozy_back.global.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
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
@RequestMapping("/memories")
public class MemoryController {

    private final MemoryService memoryService;
    private final MemoryEditLockService memoryEditLockService;

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


    // 내 기록 및 친구가 공유한 기록 조회
    @GetMapping
    public ResponseEntity<PagedResponse<MemoryInfoDto>> getMemories(
            @CurrentUserId Long userId,
            @Parameter(description = "조회할 연월", example = "2025-09")
            @RequestParam(required = false) String yearMonth,

            @Parameter(description = "카테고리", example = "TRAVEL (Null이면 전체 조회)")
            @RequestParam(required = false) MemoryCategory category,

            @Parameter(description = "페이지 번호(필터링 시 페이지 번호에 상관없이 전체 조회)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기(필터링 시 페이지 크기에 상관없이 전체 조회)", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        LocalDate from = null, to = null;
        if (yearMonth != null && !yearMonth.isBlank()) {
            YearMonth ym = YearMonth.parse(yearMonth);
            from = ym.atDay(1);
            to   = ym.atEndOfMonth();
        }

        CalendarFilter filter = CalendarFilter.builder()
                .from(from)
                .to(to)
                .memoryCategory(category)
                .build();

        boolean isFilter = from != null || category != null;

        if (isFilter) {
            return ResponseEntity.ok(memoryService.getMemoryListByFilter(userId, filter));
        }
        return ResponseEntity.ok(memoryService.getMemoryListPaged(userId, page, size, filter)); // filter는 모두 null일 수 있음
    }


    // 기록 상세 조회
    @GetMapping("/{memoryId}")
    public ResponseEntity<GetMemoryDetailsResponse> getMemoryDetails(
            @CurrentUserId Long userId,
            @PathVariable Long memoryId) {
        return ResponseEntity.ok(memoryService.getMemoryDetails(userId, memoryId));
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


    // 기록 수정 전 편집 락 획득
    @Operation(summary = "기록 편집 락 획득", description = "기록 수정 전 편집 락을 획득합니다.")
    @PostMapping("/{memoryId}/edit-session")
    public ResponseEntity<CreateEditLockResponse> acquireLock(
            @PathVariable Long memoryId,
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(memoryEditLockService.acquire(memoryId, userId));
    }

    // 편집 락 연장(하트비트)
    @Operation(summary = "기록 편집 락 연장", description = "기록 수정 중 편집 락을 3분 연장합니다.")
    @PostMapping("/{memoryId}/edit-session/heartbeat")
    public ResponseEntity<UpdateEditLockTTLResponse> heartbeat(
            @CurrentUserId Long userId,
            @PathVariable Long memoryId,
            @RequestBody UpdateEditLockRequest request) {
        return ResponseEntity.ok(memoryEditLockService.heartbeat(memoryId, userId, request.token()));
    }

    // 기록 수정 후 편집 락 해제
    @Operation(summary = "기록 편집 락 해제",
            description = "기록 수정/취소/비정상 종료 후 편집 락을 해제합니다. (+ 수정 완료되면 자동적으로 해제")
    @DeleteMapping("/{memoryId}/edit-session")
    public ResponseEntity<Void> releaseLock(
            @PathVariable Long memoryId,
            @CurrentUserId Long userId,
            @RequestBody DeleteEditLockRequest request) {
        memoryEditLockService.release(memoryId, userId, request.token());
        return ResponseEntity.noContent().build();
    }

}