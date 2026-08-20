package com.gaguraczi.paw.domain.visit.entity;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.visit.enums.AiSummaryStatus;
import com.gaguraczi.paw.domain.visit.enums.TranscriptSpeaker;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VisitTest {

    @Test
    void applyShortSummaryMarksReady() {
        Visit visit = processingVisit();

        visit.applyShortSummary(
                "관절염 정기 진료",
                "퇴행성 관절염 진단",
                List.of("파행", "관절 통증", "고령"),
                List.of("산책 조절", "체중 관리"),
                "일주일 뒤 재방문",
                "행복동물병원",
                120
        );

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.READY);
        assertThat(visit.getVisitName()).isEqualTo("관절염 정기 진료");
        assertThat(visit.getHospitalName()).isEqualTo("행복동물병원");
        assertThat(visit.getFailReason()).isNull();
    }

    @Test
    void markFailedKeepsProcessingResultOut() {
        Visit visit = processingVisit();

        visit.markFailed("음성 전사에 실패했습니다.");

        assertThat(visit.getStatus()).isEqualTo(VisitStatus.FAILED);
        assertThat(visit.getFailReason()).contains("음성 전사");
    }

    @Test
    void aiSummaryStateMachine() {
        Visit visit = processingVisit();
        visit.applyShortSummary("진료", "한줄", List.of("a", "b", "c"), List.of("care"), "", null, 10);

        visit.markAiSummaryGenerating(true);
        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.GENERATING);
        assertThat(visit.isAiSummaryCoinCharged()).isTrue();

        visit.completeAiSummary("# 상세");
        assertThat(visit.isAiSummaryDone()).isTrue();

        visit.resetAiSummary();
        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.NONE);
        assertThat(visit.isAiSummaryCoinCharged()).isFalse();
        assertThat(visit.isAiSummaryDone()).isFalse();
    }

    @Test
    void replaceTranscriptTurnsAttachesVisit() {
        Visit visit = processingVisit();
        VisitTranscriptTurn turn = VisitTranscriptTurn.builder()
                .speaker(TranscriptSpeaker.VET)
                .text("문진을 시작할게요")
                .startSec(0.0)
                .endSec(2.0)
                .sortOrder(0)
                .build();

        visit.replaceTranscriptTurns(List.of(turn));

        assertThat(visit.getTranscriptTurns()).hasSize(1);
        assertThat(visit.getTranscriptTurns().getFirst().getVisit()).isSameAs(visit);
        assertThat(visit.getTranscriptTurns().getFirst().getSpeaker()).isEqualTo(TranscriptSpeaker.VET);
    }

    private static Visit processingVisit() {
        User user = User.builder().uid(UUID.randomUUID()).build();
        Pet pet = Pet.builder()
                .petId(1L)
                .user(user)
                .petName("아리")
                .birth(LocalDate.of(2015, 3, 1))
                .petWeight(new BigDecimal("4.20"))
                .gender(Gender.FEMALE)
                .build();
        return Visit.builder()
                .visitId(1L)
                .pet(pet)
                .user(user)
                .status(VisitStatus.PROCESSING)
                .build();
    }
}
