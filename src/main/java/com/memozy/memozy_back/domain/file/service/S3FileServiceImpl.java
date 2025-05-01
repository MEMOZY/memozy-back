package com.memozy.memozy_back.domain.file.service;


import com.memozy.memozy_back.domain.file.constant.FileDomain;
import com.memozy.memozy_back.domain.file.dto.PreSignedUrlDto;
import com.memozy.memozy_back.global.exception.BusinessException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.io.InputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3FileServiceImpl implements FileService {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    private final static String FILE_PREFIX = "file";
    private final static String TEMPORARY_FILE_PREFIX = "temp";

    @Override
    public PreSignedUrlDto generatePreSignedUrl(String fileName, FileDomain fileDomain) {
        var fileKey = createFileKey(fileName, fileDomain.getDirectory(), fileDomain.isTemporary());
        var putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        var preSignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        var preSignedRequest = s3Presigner.presignPutObject(preSignRequest);

        return PreSignedUrlDto.builder()
                .preSignedUrl(preSignedRequest.url().toExternalForm())
                .fileKey(fileKey)
                .build();
    }

    @Override
    public PreSignedUrlDto generatePresignedUrlToRead(String fileKey) {
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();

        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        var presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return PreSignedUrlDto.builder()
                .fileKey(fileKey)
                .preSignedUrl(presignedRequest.url().toExternalForm())
                .build();
    }

    @Override
    public void deleteFile(String fileKey) {
        s3Client.deleteObject(builder -> builder.bucket(bucket).key(fileKey));
    }

    @Override
    public String transferToBase64(String fileKey) {
        try (InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build())) {

            byte[] bytes = inputStream.readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);

        } catch (Exception e) {
            throw new RuntimeException("S3에서 파일 다운로드 실패: " + fileKey, e);
        }
    }

    @Override
    public void validateFileKey(String fileKey) {
        if (!isUploaded(fileKey)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_RESOURCE_EXCEPTION);
        }
    }

    public boolean isUploaded(String fileKey) {
            try {
                s3Client.headObject(builder -> builder.bucket(bucket).key(fileKey));
            } catch (NoSuchKeyException e) {
            return false;
        }
        return true;
    }

    // 파일 이동
    @Override
    public String moveFile(String fileKey) {
        validateFileKey(fileKey);

        if (!fileKey.startsWith("temp/")) {
            return fileKey; // 이미 file/이면 이동할 필요 없음
        }

        String toFileKey = fileKey.replaceFirst("temp/", "file/");

        s3Client.copyObject(copy -> copy
                .sourceBucket(bucket)
                .sourceKey(fileKey)
                .destinationBucket(bucket)
                .destinationKey(toFileKey));

        s3Client.deleteObject(delete -> delete
                .bucket(bucket)
                .key(fileKey));

        return toFileKey;
    }

    private static String createFileKey(String fileName, String directory, boolean isTemporary) {
        return (isTemporary ? TEMPORARY_FILE_PREFIX : FILE_PREFIX) + "/" + directory + "/" + UUID.randomUUID()
                + fileName;
    }

}