package com.gaguraczi.paw.domain.users.entity;

import com.gaguraczi.paw.domain.breed.entity.Breed;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pet")
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long petId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Column(name = "profile_s3_key", length = 255, unique = true)
    private String profileS3Key;

    @Column(name = "profile_url", columnDefinition = "TEXT")
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type")
    private PetType petType;

    @Column(name = "pet_name", length = 255, nullable = false)
    private String petName;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "pet_weight", precision = 5, scale = 2, nullable = false)
    private BigDecimal petWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Builder.Default
    @Column(name = "neutering", nullable = false)
    private Boolean neutering = false;

    @Builder.Default
    @Column(name = "is_main")
    private boolean isMain = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id")
    private Breed breed;

    /** 표시용 품종명 (마스터 미적재·기타 선택 시) */
    @Column(name = "breed_name", length = 255)
    private String breedName;

    /** DOG는 DogBloodType, CAT은 CatBloodType의 name() 값 (petType에 따라 검증 후 저장) */
    @Column(name = "blood_type", length = 30)
    private String bloodType;

    public void setMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public void updateBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public void updateProfileImage(String profileS3Key, String profileUrl) {
        this.profileS3Key = profileS3Key;
        this.profileUrl = profileUrl;
    }

    public void update(
            PetType petType,
            Breed breed,
            String breedName,
            String petName,
            LocalDate birth,
            BigDecimal petWeight,
            Gender gender,
            Boolean neutering,
            String bloodType,
            boolean bloodTypeTouched
    ) {
        if (petType != null) {
            this.petType = petType;
        }
        if (breed != null || breedName != null) {
            this.breed = breed;
            this.breedName = breedName;
        }
        if (petName != null && !petName.isBlank()) {
            this.petName = petName.trim();
        }
        if (birth != null) {
            this.birth = birth;
        }
        if (petWeight != null) {
            this.petWeight = petWeight;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (neutering != null) {
            this.neutering = neutering;
        }
        if (bloodTypeTouched) {
            this.bloodType = bloodType;
        }
    }
}
