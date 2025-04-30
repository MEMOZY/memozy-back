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
        @Schema(
                description = "임시 Memory를 생성했을 때 발급받은 세션 ID - 세션 ID에 해당하는 Memory를 찾아 DB에 저장"
        )
        @NotBlank String sessionId,
        @NotBlank String title,
        @NotNull MemoryCategory category,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull List<MemoryItemDto> memoryItems,
        List<Long> sharedUsersId
) {}
