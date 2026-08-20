package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.res.MyTermsRes;
import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.entity.UserAgreement;
import com.gaguraczi.paw.domain.terms.repository.TermsRepository;
import com.gaguraczi.paw.domain.terms.repository.UserAgreementRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageTermsService {

    private final TermsRepository termsRepository;
    private final UserAgreementRepository userAgreementRepository;
    private final SecurityUtils securityUtils;

    public List<MyTermsRes> list() {
        User user = securityUtils.currentUser();
        Set<String> agreedKeys = new HashSet<>();
        for (UserAgreement agreement : userAgreementRepository.findByUser(user)) {
            if (agreement.isAgreed()) {
                agreedKeys.add(agreement.getTermsType().name() + ":" + agreement.getTermsVersion());
            }
        }

        List<Terms> allTerms = termsRepository.findAllByOrderByRequiredDescTypeAsc();
        return allTerms.stream()
                .map(terms -> MyTermsRes.of(terms, agreedKeys.contains(terms.getType().name() + ":" + terms.getVersion())))
                .toList();
    }
}
