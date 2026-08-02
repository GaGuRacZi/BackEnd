package com.gaguraczi.paw.domain.users.service;

import com.gaguraczi.paw.domain.users.dto.req.UserProfileUpdateReq;
import com.gaguraczi.paw.domain.users.dto.res.UserProfileRes;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.exception.code.UserErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Dto;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final SecurityUtils securityUtils;
    private final S3Utils s3Utils;

    public UserProfileRes getMyProfile() {
        return UserProfileRes.from(securityUtils.currentUser());
    }

    @Transactional
    public UserProfileRes updateMyProfile(UserProfileUpdateReq req, MultipartFile image) {
        User user = securityUtils.currentUser();

        if (req != null) {
            String name = blankToNull(req.getName());
            String nickname = blankToNull(req.getNickname());
            String intro = req.getIntro();
            user.updateProfile(name, nickname, intro);
        }

        if (image != null) {
            applyProfileImage(user, image);
        }

        return UserProfileRes.from(user);
    }

    @Transactional
    public UserProfileRes updateMyProfileImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw GeneralException.of(UserErrorCode.USER_PROFILE_IMAGE_EMPTY);
        }
        User user = securityUtils.currentUser();
        applyProfileImage(user, image);
        return UserProfileRes.from(user);
    }

    private void applyProfileImage(User user, MultipartFile image) {
        if (image.isEmpty()) {
            throw GeneralException.of(UserErrorCode.USER_PROFILE_IMAGE_EMPTY);
        }
        S3Dto uploaded = s3Utils.uploadMultipartUnderDirectory(image, "user");
        String previousKey = user.getProfileS3Key();
        try {
            user.updateProfileImage(uploaded.getKey(), uploaded.getUrl());
        } catch (RuntimeException e) {
            try {
                s3Utils.deleteFile(uploaded.getKey());
            } catch (Exception ex) {
                log.warn("Failed to cleanup uploaded user profile: {}", uploaded.getKey(), ex);
            }
            throw e;
        }
        if (previousKey != null && !previousKey.isBlank()) {
            try {
                s3Utils.deleteFile(previousKey);
            } catch (Exception ex) {
                log.warn("Failed to delete previous user profile: {}", previousKey, ex);
            }
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
