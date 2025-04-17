package com.memozy.memozy_back.domain.memory.dto.request;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record UploadPhotosRequest(
        List<MultipartFile> photos
) {

}
