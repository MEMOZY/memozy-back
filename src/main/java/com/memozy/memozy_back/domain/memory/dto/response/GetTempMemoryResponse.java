package com.memozy.memozy_back.domain.memory.dto.response;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.memory.domain.MemoryItem;
import com.memozy.memozy_back.domain.memory.dto.MemoryItemDto;
import com.memozy.memozy_back.domain.memory.dto.TempMemoryItemDto;
import java.util.List;

public record GetTempMemoryResponse(
        List<TempMemoryItemDto> memoryItems
) {
    public static GetTempMemoryResponse of(List<MemoryItem> memoryItemList, FileService fileService) {
        List<TempMemoryItemDto> items = memoryItemList.stream()
                .map(item -> TempMemoryItemDto.from(
                        item,
                        fileService.generatePresignedUrlToRead(item.getFileKey()).preSignedUrl()
                ))
                .toList();
        return new GetTempMemoryResponse(items);
    }
}