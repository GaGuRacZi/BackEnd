package com.gaguraczi.paw.domain.weights.entity;

import com.gaguraczi.paw.global.entity.Photo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pet_weight_photo")
public class PetWeightPhoto extends Photo {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_weight_id", nullable = false)
    private PetWeightEntity petWeight;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void bindPetWeight(PetWeightEntity petWeight) {
        this.petWeight = petWeight;
    }
}
