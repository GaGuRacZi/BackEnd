package com.gaguraczi.paw.domain.weights.entity;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.weights.enums.AppetiteTypeEnum;
import com.gaguraczi.paw.domain.weights.enums.BodyTypeEnum;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    @Builder.Default
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "petWeight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetWeightPhoto> photos = new ArrayList<>();

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

    public void replacePhotos(List<PetWeightPhoto> nextPhotos) {
        this.photos.clear();
        if (nextPhotos == null || nextPhotos.isEmpty()) {
            return;
        }
        for (PetWeightPhoto photo : nextPhotos) {
            photo.bindPetWeight(this);
            this.photos.add(photo);
        }
        resequencePhotos();
    }

    /**
     * keepUrls가 null이면 기존 사진은 그대로 두고 newPhotos만 추가한다(부분 수정 원칙).
     * keepUrls가 주어지면 목록에 없는 기존 사진은 제거한 뒤 newPhotos를 추가한다.
     */
    public void syncPhotos(List<String> keepUrls, List<PetWeightPhoto> newPhotos) {
        if (keepUrls != null) {
            Set<String> keepSet = new HashSet<>(keepUrls);
            this.photos.removeIf(photo -> !keepSet.contains(photo.getPhotoS3Url()));
        }
        if (newPhotos != null) {
            for (PetWeightPhoto photo : newPhotos) {
                photo.bindPetWeight(this);
                this.photos.add(photo);
            }
        }
        resequencePhotos();
    }

    private void resequencePhotos() {
        for (int i = 0; i < photos.size(); i++) {
            photos.get(i).changeSortOrder(i);
        }
    }
}
