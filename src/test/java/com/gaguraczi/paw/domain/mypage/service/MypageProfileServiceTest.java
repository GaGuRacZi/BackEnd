package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.auth.repository.OAuthRepository;
import com.gaguraczi.paw.domain.mypage.dto.res.MypageHomeRes;
import com.gaguraczi.paw.domain.notification.repository.NotificationRepository;
import com.gaguraczi.paw.domain.region.entity.LegalRegion;
import com.gaguraczi.paw.domain.region.enums.RegionLevel;
import com.gaguraczi.paw.domain.region.repository.LegalRegionRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MypageProfileServiceTest {

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private PetRepository petRepository;
    @Mock
    private OAuthRepository oAuthRepository;
    @Mock
    private LegalRegionRepository legalRegionRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private S3Utils s3Utils;

    @InjectMocks
    private MypageProfileService mypageProfileService;

    @Test
    void 홈은_시군구를_시도와_붙이고_미읽음과_구독표시명을_넣는다() {
        LegalRegion sigungu = LegalRegion.builder()
                .code("4128100000")
                .name("고양시")
                .level(RegionLevel.SIGUNGU)
                .parentCode("4100000000")
                .build();
        LegalRegion sido = LegalRegion.builder()
                .code("4100000000")
                .name("경기도")
                .level(RegionLevel.SIDO)
                .build();
        User user = User.builder()
                .uid(UUID.randomUUID())
                .name("홍길동")
                .nickname("길동이")
                .profileUrl("https://cdn.example.com/a.jpg")
                .region(sigungu)
                .subscribe(SubscribeType.BASIC)
                .build();
        when(securityUtils.currentUser()).thenReturn(user);
        when(petRepository.findFirstByUserAndIsMainTrue(user)).thenReturn(Optional.empty());
        when(legalRegionRepository.findById("4100000000")).thenReturn(Optional.of(sido));
        when(notificationRepository.countByUser_UidAndIsReadFalse(user.getUid())).thenReturn(4L);

        MypageHomeRes res = mypageProfileService.getHome();

        assertThat(res.name()).isEqualTo("홍길동");
        assertThat(res.nickname()).isEqualTo("길동이");
        assertThat(res.profileUrl()).isEqualTo("https://cdn.example.com/a.jpg");
        assertThat(res.regionName()).isEqualTo("경기도 고양시");
        assertThat(res.subscribe().plan()).isEqualTo(SubscribeType.BASIC);
        assertThat(res.subscribe().displayName()).isEqualTo("꼬마 젤리");
        assertThat(res.subscribe().active()).isTrue();
        assertThat(res.unreadNotificationCount()).isEqualTo(4L);
    }
}
