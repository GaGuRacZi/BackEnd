package com.gaguraczi.paw.domain.visit.dto.res;

import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.enums.AiSummaryStatus;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import com.gaguraczi.paw.domain.visit.support.VisitPetDisplay;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(
        name = "VisitDetailRes",
        description = """
                진료 요약 화면 데이터. 펫 프로필은 진료 시점 펫 정보입니다.
                status=PROCESSING 또는 FAILED이면 visitName, oneLineSummary, diagnosisFindings, careItems, careNote, aiSummaryMd는 비어 있습니다.
                처방 목록은 상태에 관계없이 저장된 값을 그대로 줍니다(보통 READY 이후에만 추가됨).
                """
)
public record VisitDetailRes(
        @Schema(description = "진료 ID", example = "1")
        Long visitId,
        @Schema(description = "처리 상태", example = "READY")
        VisitStatus status,
        @Schema(description = "진료 시각(업로드 createdAt)", example = "2026-08-19T13:00:00")
        LocalDateTime visitedAt,
        @Schema(description = "짧은 요약 진료명. READY가 아니면 null.", example = "스케일링", nullable = true)
        String visitName,
        @Schema(description = "펫 ID", example = "1")
        Long petId,
        @Schema(description = "펫 이름", example = "초코")
        String petName,
        @Schema(description = "품종명. 등록 품종이 있으면 그 이름, 없으면 직접 입력 품종명.", example = "말티즈", nullable = true)
        String breedName,
        @Schema(description = "나이 표시. 예: '3살 2개월', 1년 미만이면 '5개월'. 생년월일 없으면 null.", example = "3살 2개월", nullable = true)
        String petAgeLabel,
        @Schema(description = "펫 프로필 이미지 URL", example = "https://cdn.example.com/pets/1.jpg", nullable = true)
        String petProfileUrl,
        @Schema(description = "진단·소견 불릿. READY가 아니면 빈 배열.")
        List<String> diagnosisFindings,
        @Schema(description = "한 줄 요약. READY가 아니면 null.", example = "치석 제거와 잇몸 관리 안내를 받았어요.", nullable = true)
        String oneLineSummary,
        @Schema(description = "이 진료에 등록된 처방 약물")
        List<VisitPrescriptionRes> prescriptions,
        @Schema(description = "가정 케어 항목. READY가 아니면 빈 배열.")
        List<String> careItems,
        @Schema(description = "케어 메모. READY가 아니면 null.", nullable = true)
        String careNote,
        @Schema(description = "코인 AI 상세 요약 상태", example = "NONE")
        AiSummaryStatus aiSummaryStatus,
        @Schema(
                description = "AI 상세 요약 마크다운. READY이면서 이미 생성(DONE)된 경우에만 값이 있습니다. PROCESSING이면 null.",
                nullable = true
        )
        String aiSummaryMd,
        @Schema(description = "업로드한 녹음 재생 URL", example = "https://cdn.example.com/visit-audio/xxx.m4a")
        String audioUrl,
        @Schema(description = "FAILED일 때 실패 사유. 그 외에는 null.", example = "음성 전사에 실패했습니다.", nullable = true)
        String failReason
) {
    public static VisitDetailRes from(Visit visit, LocalDate today) {
        boolean ready = visit.getStatus() == VisitStatus.READY;
        return new VisitDetailRes(
                visit.getVisitId(),
                visit.getStatus(),
                visit.getCreatedAt(),
                ready ? visit.getVisitName() : null,
                visit.getPet().getPetId(),
                visit.getPet().getPetName(),
                VisitPetDisplay.breedName(visit.getPet()),
                VisitPetDisplay.ageLabel(visit.getPet().getBirth(), today),
                visit.getPet().getProfileUrl(),
                ready ? List.copyOf(visit.getDiagnosisFindings()) : List.of(),
                ready ? visit.getOneLineSummary() : null,
                visit.getPrescriptions().stream().map(VisitPrescriptionRes::from).toList(),
                ready ? List.copyOf(visit.getCareItems()) : List.of(),
                ready ? visit.getCareNote() : null,
                visit.getAiSummaryStatus(),
                ready ? visit.getAiSummaryMd() : null,
                visit.getAudioUrl(),
                visit.getFailReason()
        );
    }
}
