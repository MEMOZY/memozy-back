package com.memozy.memozy_back.domain.file.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageMetadataExtractor {

    public Metadata extract(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return ImageMetadataReader.readMetadata(inputStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("메타데이터 추출 실패", e);
        }
    }

    public LocalDateTime getTakenDate(Metadata metadata) {
        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        Date date = directory != null ? directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL) : null;
        return date != null ? LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()) : null;
    }
}