package com.gaguraczi.paw.domain.visit.entity;

import com.gaguraczi.paw.domain.medication.entity.Medication;
import com.gaguraczi.paw.domain.visit.converter.TakeTimeListConverter;
import com.gaguraczi.paw.domain.visit.enums.DoseFrequency;
import com.gaguraczi.paw.domain.visit.enums.MealTiming;
import com.gaguraczi.paw.domain.visit.enums.PrescriptionSource;
import com.gaguraczi.paw.domain.visit.enums.TakeTime;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "visit_prescription")
public class VisitPrescription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private PrescriptionSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;

    @Column(name = "name_ko", nullable = false, length = 200)
    private String nameKo;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "ingredient", columnDefinition = "TEXT")
    private String ingredient;

    @Column(name = "dosage_amount")
    private Integer dosageAmount;

    @Column(name = "dosage_unit", length = 30)
    private String dosageUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 30)
    private DoseFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_timing", nullable = false, length = 30)
    private MealTiming mealTiming;

    @Builder.Default
    @Convert(converter = TakeTimeListConverter.class)
    @Column(name = "take_times", columnDefinition = "TEXT")
    private List<TakeTime> takeTimes = new ArrayList<>();

    @Column(name = "caution", columnDefinition = "TEXT")
    private String caution;

    public void attach(Visit visit) {
        this.visit = visit;
    }
}
