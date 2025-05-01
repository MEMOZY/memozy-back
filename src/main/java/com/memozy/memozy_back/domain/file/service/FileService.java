package com.memozy.memozy_back.domain.file.service;

import com.memozy.memozy_back.domain.file.constant.FileDomain;
import com.memozy.memozy_back.domain.file.dto.PreSignedUrlDto;

public interface FileService {

    PreSignedUrlDto generatePreSignedUrl(String fileName, FileDomain fileDomain);

    PreSignedUrlDto generatePresignedUrlToRead(String fileKey);

    boolean isUploaded(String fileName);

    String moveFile(String fileKey);

    void deleteFile(String fileKey);

    String transferToBase64(String fileKey);

    void validateFileKey(String fileKey);
}