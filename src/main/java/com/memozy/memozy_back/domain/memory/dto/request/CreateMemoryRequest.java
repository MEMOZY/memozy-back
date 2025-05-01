package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.constant.MemoryCategory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreateMemoryRequest(
        @NotBlank String title,
        @NotNull MemoryCategory category,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,

        @Schema(description = "임시 저장소에 있던 memoryItems 불러오기 위함", example = "sessionId")
        @NotBlank String sessionId,

        List<Long> sharedUsersId
) {}
