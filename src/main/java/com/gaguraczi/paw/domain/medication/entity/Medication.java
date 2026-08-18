package com.gaguraczi.paw.domain.medication.entity;

import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "medication",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medication_item_seq", columnNames = "item_seq")
        },
        indexes = {
                @Index(name = "medication_name_ko_idx", columnList = "name_ko")
        }
)
public class Medication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_id")
    private Long medicationId;

    @Column(name = "item_seq", length = 20, nullable = false)
    private String itemSeq;

    @Column(name = "name_ko", length = 200, nullable = false)
    private String nameKo;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "ingredient", columnDefinition = "TEXT")
    private String ingredient;

    @Column(name = "target_animal", length = 200)
    private String targetAnimal;

    @Column(name = "description_md", nullable = false, columnDefinition = "TEXT")
    private String descriptionMd;

    @Column(name = "precaution_md", nullable = false, columnDefinition = "TEXT")
    private String precautionMd;

    @Column(name = "search_text", nullable = false, columnDefinition = "TEXT")
    private String searchText;
}
