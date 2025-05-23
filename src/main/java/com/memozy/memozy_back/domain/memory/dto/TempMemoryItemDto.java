package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import java.io.Serializable;

public record TempMemoryItemDto(
        String tempId,
        String imageUrl,
        String content,
        int sequence
) implements Serializable {

    public static TempMemoryItemDto from(MemoryItem item, String presignedUrl) {
        return new TempMemoryItemDto(
                item.getTempId(),
                presignedUrl,
                item.getContent(),
                item.getSequence()
        );
    }
}