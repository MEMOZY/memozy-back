package com.memozy.memozy_back.domain.memory.dto.response;

import com.memozy.memozy_back.domain.memory.dto.PhotoInfo;
import java.util.List;

public record GetUploadedPhotoInfoListResponse(
    List<PhotoInfo> memoryPhotosInfoList
) {
    public static GetUploadedPhotoInfoListResponse from(List<PhotoInfo> memoryPhotosInfoList) {
        return new GetUploadedPhotoInfoListResponse(memoryPhotosInfoList);
    }
}
