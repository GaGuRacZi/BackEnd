package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.enums.AiSummaryStatus;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.repository.VisitRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitAiSummaryTxService {

    public enum ReserveResult {
        ALREADY_DONE,
        RESERVED
    }

    private final VisitRepository visitRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReserveResult reserve(Long visitId, UUID uid, int cost) {
        Visit visit = visitRepository.findByIdForUpdate(visitId)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        if (visit.isAiSummaryDone()) {
            return ReserveResult.ALREADY_DONE;
        }
        if (visit.getAiSummaryStatus() == AiSummaryStatus.GENERATING) {
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_CONFLICT);
        }
        if (visit.getStatus() != VisitStatus.READY) {
            throw GeneralException.of(VisitErrorCode.VISIT_NOT_READY);
        }
        visit.markAiSummaryGenerating();
        User user = userRepository.findByIdForUpdate(uid)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        if (user.coinBalance() < cost) {
            throw GeneralException.of(VisitErrorCode.VISIT_COIN_INSUFFICIENT);
        }
        user.deductCoin(cost);
        return ReserveResult.RESERVED;
    }

    @Transactional
    public void complete(Long visitId, String markdown) {
        Visit visit = visitRepository.findByIdForUpdate(visitId)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        visit.completeAiSummary(markdown);
    }

    @Transactional
    public void refund(Long visitId, UUID uid, int cost) {
        Visit visit = visitRepository.findByIdForUpdate(visitId)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        User user = userRepository.findByIdForUpdate(uid)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        if (visit.getAiSummaryStatus() != AiSummaryStatus.GENERATING) {
            log.warn(
                    "Skip AI summary refund visitId={} status={} uid={}",
                    visitId,
                    visit.getAiSummaryStatus(),
                    uid
            );
            return;
        }
        visit.resetAiSummary();
        user.refundCoin(cost);
    }
}
