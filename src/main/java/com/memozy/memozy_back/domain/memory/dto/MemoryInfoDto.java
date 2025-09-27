package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import java.time.LocalDate;

public record MemoryInfoDto(
        Long id,
        Long ownerId,
        String title,
        String content,
        LocalDate startDate,
        LocalDate endDate,
        String thumbnailUrl,
        PermissionLevel permissionLevel,
        boolean canEdit
) {
}
