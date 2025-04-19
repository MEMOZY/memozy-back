package com.memozy.memozy_back.domain.file.service;

import com.memozy.memozy_back.domain.file.constant.FileDomain;
import com.memozy.memozy_back.domain.file.dto.PreSignedUrlDto;

public interface FileService {

    PreSignedUrlDto generatePreSignedUrl(String fileName, FileDomain fileDomain);

    boolean isUploaded(String fileName);

    String moveFile(String imageUrl);

    void deleteFile(String fileKey);
}