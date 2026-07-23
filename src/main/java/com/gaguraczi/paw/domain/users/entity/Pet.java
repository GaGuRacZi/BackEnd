package com.gaguraczi.paw.domain.users.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "pet_weight", precision = 3, scale = 2, nullable = false)
    private BigDecimal petWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Builder.Default
    @Column(name = "neutering", nullable = false)
    private Boolean neutering = false;

    @Builder.Default
    @Column(name = "is_main")
    private Boolean isMain = false;

    @Column(name = "breed", length = 255)
    private String breed;
}