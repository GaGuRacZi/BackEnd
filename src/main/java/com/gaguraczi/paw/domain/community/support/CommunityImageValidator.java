package com.gaguraczi.paw.domain.community.support;

import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Validates community image uploads by size and Apache Tika content detection.
 */
public final class CommunityImageValidator {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    public static final int MAX_PHOTOS = 5;

    private static final Tika TIKA = new Tika();

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/heic",
            "image/heif"
    );

    private CommunityImageValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw GeneralException.of(CommunityErrorCode.PHOTO_EMPTY_400);
        }
        if (file.getSize() > MAX_BYTES) {
            throw GeneralException.of(CommunityErrorCode.PHOTO_TOO_LARGE_400);
        }

        String detectedType;
        try (InputStream in = file.getInputStream()) {
            detectedType = TIKA.detect(in, file.getOriginalFilename());
        } catch (IOException e) {
            throw GeneralException.of(CommunityErrorCode.PHOTO_INVALID_400, e);
        }

        if (detectedType == null || !ALLOWED_MIME_TYPES.contains(detectedType.toLowerCase())) {
            throw GeneralException.of(CommunityErrorCode.PHOTO_INVALID_400);
        }
    }
}
