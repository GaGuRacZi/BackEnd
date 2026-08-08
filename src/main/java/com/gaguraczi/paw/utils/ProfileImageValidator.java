package com.gaguraczi.paw.utils;

import com.gaguraczi.paw.domain.users.exception.code.UserErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Validates profile-image uploads by size and Apache Tika content detection (not request Content-Type).
 */
public final class ProfileImageValidator {

    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Tika TIKA = new Tika();

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/heic",
            "image/heif"
    );

    private ProfileImageValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw GeneralException.of(UserErrorCode.USER_PROFILE_IMAGE_EMPTY);
        }
        if (file.getSize() > MAX_BYTES) {
            throw GeneralException.of(UserErrorCode.USER_PROFILE_IMAGE_TOO_LARGE);
        }

        String detectedType;
        try (InputStream in = file.getInputStream()) {
            detectedType = TIKA.detect(in, file.getOriginalFilename());
        } catch (IOException e) {
            throw GeneralException.of(UserErrorCode.USER_PROFILE_IMAGE_INVALID, e);
        }

        if (detectedType == null || !ALLOWED_MIME_TYPES.contains(detectedType.toLowerCase())) {
            throw GeneralException.of(UserErrorCode.USER_PROFILE_IMAGE_INVALID);
        }
    }
}
