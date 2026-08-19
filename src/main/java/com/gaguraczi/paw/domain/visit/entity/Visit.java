package com.gaguraczi.paw.domain.visit.entity;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.visit.enums.AiSummaryStatus;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import com.gaguraczi.paw.global.converter.StringListConverter;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "visit",
        indexes = {
                @Index(name = "idx_visit_pet_created", columnList = "pet_id, created_at"),
                @Index(name = "idx_visit_uid_created", columnList = "uid, created_at")
        }
)
public class Visit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long visitId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Column(name = "visit_name", length = 200)
    private String visitName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private VisitStatus status = VisitStatus.PROCESSING;

    @Column(name = "one_line_summary", length = 500)
    private String oneLineSummary;

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "diagnosis_findings", columnDefinition = "TEXT")
    private List<String> diagnosisFindings = new ArrayList<>();

    @Builder.Default
    @Convert(converter = StringListConverter.class)
    @Column(name = "care_items", columnDefinition = "TEXT")
    private List<String> careItems = new ArrayList<>();

    @Column(name = "care_note", length = 500)
    private String careNote;

    @Column(name = "hospital_name", length = 200)
    private String hospitalName;

    @Column(name = "audio_s3_key", length = 255)
    private String audioS3Key;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    @Column(name = "audio_duration_sec")
    private Integer audioDurationSec;

    @Column(name = "audio_content_type", length = 100)
    private String audioContentType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "ai_summary_status", nullable = false, length = 20)
    private AiSummaryStatus aiSummaryStatus = AiSummaryStatus.NONE;

    @Column(name = "ai_summary_md", columnDefinition = "TEXT")
    private String aiSummaryMd;

    @Column(name = "ai_summary_generated_at")
    private LocalDateTime aiSummaryGeneratedAt;

    @Column(name = "fail_reason", columnDefinition = "TEXT")
    private String failReason;

    @Builder.Default
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 50)
    private List<VisitTranscriptTurn> transcriptTurns = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("prescriptionId ASC")
    @BatchSize(size = 20)
    private List<VisitPrescription> prescriptions = new ArrayList<>();

    public void applyShortSummary(
            String visitName,
            String oneLineSummary,
            List<String> diagnosisFindings,
            List<String> careItems,
            String careNote,
            String hospitalName,
            Integer audioDurationSec
    ) {
        this.visitName = visitName;
        this.oneLineSummary = oneLineSummary;
        this.diagnosisFindings = diagnosisFindings == null ? new ArrayList<>() : new ArrayList<>(diagnosisFindings);
        this.careItems = careItems == null ? new ArrayList<>() : new ArrayList<>(careItems);
        this.careNote = careNote;
        this.hospitalName = hospitalName;
        if (audioDurationSec != null) {
            this.audioDurationSec = audioDurationSec;
        }
        this.status = VisitStatus.READY;
        this.failReason = null;
    }

    public void replaceTranscriptTurns(List<VisitTranscriptTurn> turns) {
        this.transcriptTurns.clear();
        if (turns == null) {
            return;
        }
        for (VisitTranscriptTurn turn : turns) {
            turn.attach(this);
            this.transcriptTurns.add(turn);
        }
    }

    public void markFailed(String reason) {
        this.status = VisitStatus.FAILED;
        this.failReason = reason == null ? null : trimTo(reason, 2000);
    }

    public void markAiSummaryGenerating() {
        this.aiSummaryStatus = AiSummaryStatus.GENERATING;
    }

    public void completeAiSummary(String markdown) {
        this.aiSummaryMd = markdown;
        this.aiSummaryStatus = AiSummaryStatus.DONE;
        this.aiSummaryGeneratedAt = LocalDateTime.now();
    }

    public void resetAiSummary() {
        this.aiSummaryStatus = AiSummaryStatus.NONE;
        this.aiSummaryMd = null;
        this.aiSummaryGeneratedAt = null;
    }

    public boolean isAiSummaryDone() {
        return aiSummaryStatus == AiSummaryStatus.DONE && aiSummaryMd != null && !aiSummaryMd.isBlank();
    }

    private static String trimTo(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
