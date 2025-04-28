package com.memozy.memozy_back.domain.file.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record GeneratePreSignedUrlsRequest(
        @NotEmpty
        List<@Valid GeneratePreSignedUrlRequest> fileInfos
) {
}
