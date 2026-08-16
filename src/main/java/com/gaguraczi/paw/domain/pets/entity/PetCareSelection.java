package com.gaguraczi.paw.domain.pets.entity;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** 반려동물이 선택한 원료 제외/수술 이력/관리 부위 코드 매핑 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "pet_care_selection",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pet_care_selection_pet_code",
                columnNames = {"pet_id", "pet_care_code_id"}
        )
)
public class PetCareSelection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_care_selection_id")
    private Long petCareSelectionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_care_code_id", nullable = false)
    private PetCareCode code;
}
