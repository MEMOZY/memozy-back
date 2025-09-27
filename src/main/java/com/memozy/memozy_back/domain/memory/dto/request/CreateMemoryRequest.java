package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.constant.MemoryCategory;
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

        @Schema(description = "권한 부여에 대한 정보 목록 (비어있을 수 있음)")
        List<AccessGrantRequest> accesses
) {
}
