package com.memozy.memozy_back.domain.user.service;

import com.memozy.memozy_back.domain.file.service.FileService;
import com.memozy.memozy_back.domain.user.constant.ProfileImage;
import com.memozy.memozy_back.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final FileService fileService;

    public String generatePresignedUrlToRead(String profileImageUrl) {
        String defaultImageUrl = ProfileImage.DEFAULT.getUrl();
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            log.warn("Profile image URL is null or blank");
            return defaultImageUrl;
        }

        if (isExternalUrl(profileImageUrl)) {
            // 외부 소셜 URL은 그대로 사용
            return profileImageUrl;
        }

        try {
            String fileKey = fileService.extractFileKeyFromImageUrl(profileImageUrl);

            if (fileService.isUploaded(fileKey)) {
                return fileService.generatePresignedUrlToRead(fileKey).preSignedUrl();
            } else {
                log.warn("S3 file not found: {}", fileKey);
                return defaultImageUrl;
            }
        } catch (BusinessException e) {
            log.warn("Invalid image URL provided: {}", profileImageUrl, e);
            return defaultImageUrl;
        } catch (Exception e) {
            log.error("Unexpected error generating presigned URL for: {}", profileImageUrl, e);
            return defaultImageUrl;
        }
    }

    private boolean isExternalUrl(String url) {
        return url.startsWith("http://k.kakaocdn.net/") ||
                url.startsWith("https://lh3.googleusercontent.com/");
    }
}