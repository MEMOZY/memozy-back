package com.memozy.memozy_back.domain.memory.dto;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.user.domain.User;
import java.io.Serializable;
import java.util.List;

public record TempMemoryDto(
        Long ownerId,
        List<TempMemoryItemDto> memoryItems
) implements Serializable {
    public static TempMemoryDto from(Memory memory, List<TempMemoryItemDto> items) {
        return new TempMemoryDto(memory.getOwner().getId(), items);
    }

    public Memory toDomain(User owner, FileService fileService) {
        Memory memory = Memory.createWithoutBasicInfo(owner);

        for (TempMemoryItemDto itemDto : memoryItems) {
            memory.addMemoryItem(MemoryItem.fromTempDto(
                    memory,
                    fileService.extractFileKeyFromImageUrl(itemDto.imageUrl()),
                    itemDto.content(),
                    itemDto.sequence(),
                    itemDto.tempId()
            ));
        }
        return memory;
    }
}
