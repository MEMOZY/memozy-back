package com.memozy.memozy_back.domain.file.controller;

import com.memozy.memozy_back.domain.file.dto.PreSignedUrlDto;
import com.memozy.memozy_back.domain.file.dto.request.GeneratePreSignedUrlsRequest;
import com.memozy.memozy_back.domain.file.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "파일 업로드 API", description = "S3 관리")
@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/pre-signed-urls")
    public ResponseEntity<List<PreSignedUrlDto>> generatePreSignedUrl(
            @Valid @RequestBody GeneratePreSignedUrlsRequest requests) {

        var result = requests.fileInfos().stream()
                .map(request -> fileService.generatePreSignedUrl(request.fileName(), request.fileDomain()))
                .toList();
        return ResponseEntity.ok(result);
    }
}