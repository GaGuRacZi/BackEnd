package com.gaguraczi.paw.domain.walk.entity;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.walk.enums.WalkStatusEnum;
import com.gaguraczi.paw.domain.walk.enums.WalkTypeEnum;
import com.gaguraczi.paw.domain.walk.enums.WeatherTypeEnum;
import com.gaguraczi.paw.domain.walkcourse.entity.WalkCourseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

//산책 기록
@Entity
@Table(
        name = "walk",
        indexes = {
                // "이 반려동물의 이 날짜 기록" 조회가 제일 많아서 인덱스를 걸어뒀어요
                @Index(name = "idx_walk_pet_date", columnList = "pet_id, walk_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "walk_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private WalkCourseEntity walkCourse;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_type", nullable = false, length = 20)
    private WeatherTypeEnum weatherType;


    @Column(name = "walking_amount", nullable = false, precision = 3, scale = 1)
    private BigDecimal walkingAmount;


    @Enumerated(EnumType.STRING)
    @Column(name = "walk_type", nullable = false, length = 20)
    private WalkTypeEnum walkType;


    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;


    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "walk_date", nullable = false)
    private LocalDate walkDate;


    @Column(name = "temp", nullable = false)
    private Integer temp;


    @Column(name = "is_stool", nullable = false)
    private Boolean isStool;


    @Column(name = "is_urine", nullable = false)
    private Boolean isUrine;


    @Column(name = "significant", columnDefinition = "TEXT")
    private String significant;


    @Enumerated(EnumType.STRING)
    @Column(name = "walk_status", nullable = false, length = 20)
    private WalkStatusEnum walkStatus;

    @Builder
    private WalkEntity(Pet pet,
                 WalkCourseEntity walkCourse,
                 WeatherTypeEnum weatherType,
                 BigDecimal walkingAmount,
                 WalkTypeEnum walkType,
                 LocalDateTime startTime,
                 LocalDateTime endTime,
                 LocalDate walkDate,
                 Integer temp,
                 Boolean isStool,
                 Boolean isUrine,
                 String significant,
                 WalkStatusEnum walkStatus) {
        this.pet = pet;
        this.walkCourse = walkCourse;
        this.weatherType = weatherType;
        this.walkingAmount = (walkingAmount != null) ? walkingAmount : BigDecimal.ZERO;
        this.walkType = (walkType != null) ? walkType : WalkTypeEnum.NORMAL;
        this.startTime = startTime;
        this.endTime = endTime;
        this.walkDate = walkDate;
        this.temp = temp;
        this.isStool = (isStool != null) ? isStool : Boolean.FALSE;   // NOT NULL DEFAULT false
        this.isUrine = (isUrine != null) ? isUrine : Boolean.FALSE;
        this.significant = significant;
        this.walkStatus = (walkStatus != null) ? walkStatus : WalkStatusEnum.COMPLETED;
    }


    public void finish(LocalDateTime endTime,
                       WalkCourseEntity walkCourse,
                       BigDecimal walkingAmount,
                       WalkTypeEnum walkType,
                       Boolean isStool,
                       Boolean isUrine,
                       String significant) {
        this.endTime = endTime;
        if (walkCourse != null) this.walkCourse = walkCourse;
        if (walkingAmount != null) this.walkingAmount = walkingAmount;
        if (walkType != null) this.walkType = walkType;
        if (isStool != null) this.isStool = isStool;
        if (isUrine != null) this.isUrine = isUrine;
        if (significant != null) this.significant = significant;
        this.walkStatus = WalkStatusEnum.COMPLETED;
    }

    public void update(WalkCourseEntity walkCourse,
                       WeatherTypeEnum weatherType,
                       BigDecimal walkingAmount,
                       WalkTypeEnum walkType,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       LocalDate walkDate,
                       Integer temp,
                       Boolean isStool,
                       Boolean isUrine,
                       String significant) {
        if (walkCourse != null) this.walkCourse = walkCourse;
        if (weatherType != null) this.weatherType = weatherType;
        if (walkingAmount != null) this.walkingAmount = walkingAmount;
        if (walkType != null) this.walkType = walkType;
        if (startTime != null) this.startTime = startTime;
        if (endTime != null) this.endTime = endTime;
        if (walkDate != null) this.walkDate = walkDate;
        if (temp != null) this.temp = temp;
        if (isStool != null) this.isStool = isStool;
        if (isUrine != null) this.isUrine = isUrine;
        if (significant != null) this.significant = significant;
    }


    public boolean isCompleted() {
        return this.walkStatus == WalkStatusEnum.COMPLETED;
    }


    public Long getDurationMinutes() {
        if (startTime == null || endTime == null) {
            return null; // 아직 진행 중
        }
        return Duration.between(startTime, endTime).toMinutes();
    }
}
