package com.memozy.memozy_back.domain.file.service;


import com.memozy.memozy_back.domain.file.constant.FileDomain;
import com.memozy.memozy_back.domain.file.dto.PreSignedUrlDto;
import com.memozy.memozy_back.global.exception.GlobalException;
import com.memozy.memozy_back.global.exception.ErrorCode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3FileServiceImpl implements FileService {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    private final static String PERMANENT_DIR_PREFIX = "file";
    private final static String TEMPORARY_DIR_PREFIX = "temp";

    private static final List<String> SUPPORTED_IMAGE_EXTENSIONS
            = List.of("jpg", "jpeg", "png", "gif", "webp");

    @Override
    public PreSignedUrlDto generatePreSignedUrl(String fileName, FileDomain fileDomain) {
        var fileKey = createFileKey(fileName, fileDomain.getDirectory(), fileDomain.isTemporary());

        var putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build(); //contentType 제거

        var preSignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        var preSignedRequest = s3Presigner.presignPutObject(preSignRequest);

        return PreSignedUrlDto.builder()
                .preSignedUrl(preSignedRequest.url().toExternalForm())
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
            throw new GlobalException(ErrorCode.NOT_FOUND_S3_RESOURCE_EXCEPTION);
        }
    }

    @Override
    public String convertHeicIfNeeded(String fileKey) {
        String ext = getFileExtension(fileKey).toLowerCase();
        if (!ext.equals("heic")) return fileKey;
        return convertHeicToJpegIfNeeded(fileKey); // 내부에 변환 + 업로드 + 삭제 포함
    }

    @Override
    public String saveImageToS3(String externalImageUrl) {
        if (externalImageUrl.isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String ext = extractExtFromUrlOrDefault(externalImageUrl, ".jpg");
        String fileKey = createFileKey("profile" + ext, "user/profile", false);

        byte[] bytes = downloadExternalImage(externalImageUrl);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileKey)
                        .contentType(guessContentTypeFromExt(ext))
                        .build(),
                RequestBody.fromBytes(bytes)
        );

        return fileKey;
    }


    private String convertHeicToJpegIfNeeded(String fileKey) {
        try {
            if (!fileKey.toLowerCase().endsWith(".heic")) {
                return fileKey; // 변환 필요 없음
            }

            // 1. S3에서 HEIC 파일 다운로드
            InputStream heicInputStream = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build());

            File heicFile = File.createTempFile("image", ".heic");
            Files.copy(heicInputStream, heicFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File jpegFile = File.createTempFile("image", ".jpg");

            // 2. ImageMagick 변환
            ProcessBuilder pb = new ProcessBuilder("magick", "-define", "heic:support-auxiliary=false", heicFile.getAbsolutePath(), jpegFile.getAbsolutePath());
            pb.redirectErrorStream(true); // stderr도 stdout으로 합치기
            Process process = pb.start();

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            log.info("ImageMagick 실행 결과:\n{}", output);

            if (exitCode != 0 || !jpegFile.exists() || jpegFile.length() == 0) {
                if (!heicFile.delete()) {
                    log.warn("HEIC 임시 파일 삭제 실패: {}", heicFile.getAbsolutePath());
                }
                if (!jpegFile.delete()) {
                    log.warn("JPEG 임시 파일 삭제 실패: {}", jpegFile.getAbsolutePath());
                }
                throw new GlobalException(ErrorCode.IMAGE_CONVERSION_FAILED);
            }

            // 3. JPEG 파일 S3에 재업로드
            String newKey = fileKey.replaceAll("(?i)\\.heic$", ".jpg");
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(newKey).build(),
                    RequestBody.fromFile(jpegFile)
            );

            // 4. 원본 HEIC 삭제
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(fileKey).build());

            if (!heicFile.delete()) {
                log.warn("HEIC 임시 파일 삭제 실패: {}", heicFile.getAbsolutePath());
            }
            if (!jpegFile.delete()) {
                log.warn("JPEG 임시 파일 삭제 실패: {}", jpegFile.getAbsolutePath());
            }

            return newKey;

        } catch (IOException | InterruptedException e) {
            log.error("HEIC 변환 중 예외 발생", e);
            throw new GlobalException(ErrorCode.IMAGE_CONVERSION_FAILED);
        } catch (Exception e) {
            log.error("HEIC 변환 중 알 수 없는 예외", e);
            throw new GlobalException(ErrorCode.IMAGE_CONVERSION_FAILED);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";  // 확장자 없음
        }
        return filename.substring(lastDot + 1);
    }

    public boolean isUploaded(String fileKey) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucket).key(fileKey));
        } catch (NoSuchKeyException e) {
            return false;
        }
        return true;
    }

    @Override
    public String extractFileKeyFromImageUrl(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            String path = uri.getPath(); // 예: /temp/memory/abc.jpg
            if (path.startsWith("/")) {
                path = path.substring(1); // 슬래시 제거 → temp/memory/abc.jpg
            }
            return path;
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.INVALID_INPUT_VALUE);
        }
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
        return (isTemporary ? TEMPORARY_DIR_PREFIX : PERMANENT_DIR_PREFIX) + "/"
                + directory + "/"
                + UUID.randomUUID()
                + fileName;
    }

    private String extractExtFromUrlOrDefault(String url, String defaultExt) {
        try {
            String lower = url.toLowerCase();
            if (lower.contains(".png")) return ".png";
            if (lower.contains(".webp")) return ".webp";
            if (lower.contains(".gif")) return ".gif";
            if (lower.contains(".jpeg")) return ".jpeg";
            if (lower.contains(".jpg")) return ".jpg";
            return defaultExt;
        } catch (Exception e) {
            return defaultExt;
        }
    }

    private String guessContentTypeFromExt(String ext) {
        return switch (ext) {
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".gif" -> "image/gif";
            case ".jpeg", ".jpg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private byte[] downloadExternalImage(String url) {
        try (InputStream in = URI.create(url).toURL().openStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            log.error("외부 이미지 다운로드 실패: {}", url, e);
            return new byte[0];
        }
    }

}