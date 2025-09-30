package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.constant.PermissionLevel;
import java.util.List;
import java.util.Map;

public record MemorySharedEvent(
        Long memoryId,
        Long actorUserId,
        String actorUserNickName,
        List<Long> recipientIds
) {
}
