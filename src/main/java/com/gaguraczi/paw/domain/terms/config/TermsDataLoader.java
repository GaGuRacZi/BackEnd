package com.gaguraczi.paw.domain.terms.config;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import com.gaguraczi.paw.domain.terms.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Order(50)
@RequiredArgsConstructor
public class TermsDataLoader implements ApplicationRunner {

    private static final String VERSION = "1.0.0";
    private static final LocalDate EFFECTIVE_AT = LocalDate.of(2026, 7, 23);

    private final TermsRepository termsRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List.of(
                seed(TermsType.AGE_OVER_14, "만 14세 이상 확인", true,
                        "만 14세 이상임을 확인합니다."),
                seed(TermsType.TERMS_OF_SERVICE, "서비스 이용약관", true,
                        """
                        제1조 (목적)
                        본 약관은 PAW Project Team(이하 "운영자")이 제공하는 반려동물 건강 기록 및 생활 관리 서비스 PAW(이하 "서비스")의 이용조건 및 절차를 규정합니다.

                        제2조 (서비스의 내용)
                        서비스는 반려동물 프로필, 일정/할 일, 건강·투약·진료 기록, 알림, 위치 기반 지역 설정, 커뮤니티·마켓·리뷰·채팅, 진료 음성 기록·전사·AI 요약·OCR, 유료 구독 기능 등을 포함할 수 있습니다.

                        제3조 (회원가입 및 계정)
                        회원은 이메일 또는 소셜 로그인을 통해 가입할 수 있으며, 정확한 정보를 제공하고 계정을 안전하게 관리해야 합니다.
                        """),
                seed(TermsType.PRIVACY, "개인정보 수집·이용 동의", true,
                        "서비스 제공을 위해 이메일, 이름, 닉네임, 프로필 정보, 반려동물 정보, 위치 정보를 수집·이용합니다."),
                seed(TermsType.PROFILE_EXTRA, "프로필 추가정보 수집·이용 동의", true,
                        "맞춤형 서비스 제공을 위해 한줄소개, 프로필 사진, 반려동물 상세 정보 등 추가 정보를 수집·이용합니다."),
                seed(TermsType.MARKETING_PUSH, "마케팅 정보 수신 동의(앱 푸시)", false,
                        "이벤트 및 혜택 안내를 앱 푸시로 수신하는 것에 동의합니다. (선택)"),
                seed(TermsType.LOCATION_SERVICE, "위치기반 서비스 이용약관", false,
                        "지역 기반 병원 검색 및 알림 제공을 위해 위치정보를 이용합니다. (선택)")
        ).forEach(this::saveIfAbsent);

        log.info("Terms seed ensured. count={}", termsRepository.count());
    }

    private Terms seed(TermsType type, String title, boolean required, String content) {
        return Terms.builder()
                .type(type)
                .title(title)
                .content(content)
                .version(VERSION)
                .required(required)
                .effectiveAt(EFFECTIVE_AT)
                .build();
    }

    private void saveIfAbsent(Terms terms) {
        if (!termsRepository.existsByTypeAndVersion(terms.getType(), terms.getVersion())) {
            termsRepository.save(terms);
        }
    }
}
