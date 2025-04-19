package com.memozy.memozy_back.domain.file.controller;

import com.memozy.memozy_back.domain.file.dto.request.GeneratePreSignedUrlRequest;
import com.memozy.memozy_back.domain.file.dto.PreSignedUrlDto;
import com.memozy.memozy_back.domain.file.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/pre-signed-url")
    public ResponseEntity<PreSignedUrlDto> generatePreSignedUrl(
            @Valid @RequestBody GeneratePreSignedUrlRequest request) {
        var result = fileService.generatePreSignedUrl(request.fileName(), request.fileDomain());
        return ResponseEntity.ok(result);
    }
}