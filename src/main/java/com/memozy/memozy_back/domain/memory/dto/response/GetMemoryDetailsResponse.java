package com.memozy.memozy_back.domain.memory.dto.response;

import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import com.memozy.memozy_back.domain.memory.dto.MemoryDto;

public record GetMemoryDetailsResponse(
        MemoryDto memoryDetails,
        PermissionLevel permissionLevel,
        boolean canEdit
) {

}
