package com.gaguraczi.paw.domain.terms.service;

import com.gaguraczi.paw.domain.terms.dto.res.TermsDetailRes;
import com.gaguraczi.paw.domain.terms.dto.res.TermsSummaryRes;
import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.entity.UserAgreement;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import com.gaguraczi.paw.domain.terms.exception.code.TermsErrorCode;
import com.gaguraczi.paw.domain.terms.repository.TermsRepository;
import com.gaguraczi.paw.domain.terms.repository.UserAgreementRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserAgreementRepository userAgreementRepository;

    public List<TermsSummaryRes> list() {
        return termsRepository.findAllByOrderByRequiredDescTypeAsc().stream()
                .map(TermsSummaryRes::from)
                .toList();
    }

    public TermsDetailRes detail(TermsType type) {
        Terms terms = termsRepository.findFirstByTypeOrderByEffectiveAtDesc(type)
                .orElseThrow(() -> GeneralException.of(TermsErrorCode.TERMS_NOT_FOUND));
        return TermsDetailRes.from(terms);
    }

    @Transactional
    public void saveAgreements(User user, Map<TermsType, Boolean> agreements) {
        List<Terms> allTerms = termsRepository.findAllByOrderByRequiredDescTypeAsc();
        if (allTerms.isEmpty()) {
            throw GeneralException.of(TermsErrorCode.TERMS_NOT_FOUND);
        }

        Map<TermsType, Boolean> resolved = new EnumMap<>(TermsType.class);
        if (agreements != null) {
            resolved.putAll(agreements);
        }

        for (Terms terms : allTerms) {
            boolean agreed = Boolean.TRUE.equals(resolved.get(terms.getType()));
            if (terms.isRequired() && !agreed) {
                throw GeneralException.of(TermsErrorCode.TERMS_REQUIRED);
            }
            if (!agreed) {
                continue;
            }
            userAgreementRepository.save(UserAgreement.builder()
                    .user(user)
                    .termsType(terms.getType())
                    .termsVersion(terms.getVersion())
                    .agreed(true)
                    .agreedAt(LocalDateTime.now())
                    .build());
        }
    }
}
