package com.memozy.memozy_back.domain.memory.dto.request;

import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import jakarta.validation.constraints.NotNull;

public record AccessGrantRequest(
        @NotNull Long userId,
        @NotNull PermissionLevel permissionLevel
) {
}
