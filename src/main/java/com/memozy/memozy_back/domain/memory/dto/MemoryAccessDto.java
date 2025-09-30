package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import com.memozy.memozy_back.domain.memory.domain.MemoryAccess;

public record MemoryAccessDto(
        Long userId,
        String username,
        PermissionLevel permissionLevel
) {
    public static MemoryAccessDto of(MemoryAccess memoryAccess) {

        return new MemoryAccessDto(memoryAccess.getUser().getId(), memoryAccess.getUser().getUsername(), memoryAccess.getPermissionLevel());
    }
}
