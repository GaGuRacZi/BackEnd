package com.gaguraczi.paw.domain.pets.entity;

import com.gaguraczi.paw.domain.pets.enums.PetCareCategory;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 먹거리(피해야 할 원료)·수술 이력·관리 부위 마스터 코드. species가 null이면 종 공통. */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "pet_care_code",
        indexes = @Index(name = "idx_pet_care_code_category_species", columnList = "category, species")
)
public class PetCareCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_care_code_id")
    private Long petCareCodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private PetCareCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "species", length = 10)
    private PetType species;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
