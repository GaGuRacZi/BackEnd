package com.gaguraczi.paw.domain.weights.entity;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.weights.enums.AppetiteTypeEnum;
import com.gaguraczi.paw.domain.weights.enums.BodyTypeEnum;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "pet_weight",
        indexes = @Index(name = "idx_pet_weight_pet_recorded", columnList = "pet_id, create_date")
)
public class PetWeightEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_weight_id")
    private Long petWeightId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", nullable = false)
    private BodyTypeEnum bodyType;


    @Enumerated(EnumType.STRING)
    @Column(name = "appetite_type", nullable = false)
    private AppetiteTypeEnum appetiteType;


    @Column(name = "memo_content", columnDefinition = "TEXT")
    private String memoContent;


    @Column(name = "create_date", nullable = false)
    private LocalDateTime recordedAt;

    public void update(
            BigDecimal weight,
            BodyTypeEnum bodyType,
            AppetiteTypeEnum appetiteType,
            String memoContent,
            LocalDateTime recordedAt
    ) {
        if (weight != null) {
            this.weight = weight;
        }
        if (bodyType != null) {
            this.bodyType = bodyType;
        }
        if (appetiteType != null) {
            this.appetiteType = appetiteType;
        }
        if (memoContent != null) {
            this.memoContent = memoContent.isBlank() ? null : memoContent.trim();
        }
        if (recordedAt != null) {
            this.recordedAt = recordedAt;
        }
    }
}
