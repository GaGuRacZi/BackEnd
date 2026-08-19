package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.auth.entity.OAuth;
import com.gaguraczi.paw.domain.auth.repository.OAuthRepository;
import com.gaguraczi.paw.domain.mypage.dto.res.MypageHomeRes;
import com.gaguraczi.paw.domain.mypage.dto.res.MypageProfileRes;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.region.repository.LegalRegionRepository;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageProfileService {

    private final SecurityUtils securityUtils;
    private final PetRepository petRepository;
    private final OAuthRepository oAuthRepository;
    private final LegalRegionRepository legalRegionRepository;
    private final S3Utils s3Utils;

    public MypageHomeRes getHome() {
        User user = securityUtils.currentUser();
        Pet mainPet = petRepository.findFirstByUserAndIsMainTrue(user).orElse(null);
        return MypageHomeRes.of(user, mainPet);
    }

    public MypageProfileRes getProfile() {
        User user = securityUtils.currentUser();
        List<OAuth> linkedAccounts = oAuthRepository.findAllByUser(user);
        return MypageProfileRes.of(user, linkedAccounts);
    }

    @Transactional
    public void deleteProfileImage() {
        User user = securityUtils.currentUser();
        String previousKey = user.getProfileS3Key();
        user.updateProfileImage(null, null);
        s3Utils.scheduleDeleteAfterCommit(previousKey);
    }

    @Transactional
    public void updateRegion(String regionCode) {
        User user = securityUtils.currentUser();
        LegalRegion region = legalRegionRepository.findById(regionCode)
                .orElseThrow(() -> GeneralException.of(MypageErrorCode.REGION_NOT_FOUND));
        user.updateLocation(null, region, region.getName());
    }
}
