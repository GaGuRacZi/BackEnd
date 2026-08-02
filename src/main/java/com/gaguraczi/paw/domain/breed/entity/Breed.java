package com.gaguraczi.paw.domain.breed.entity;

import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "breed",
        indexes = {
                @Index(name = "idx_breed_pet_type_name", columnList = "pet_type, name"),
                @Index(name = "idx_breed_popular", columnList = "pet_type, is_popular")
        }
)
public class Breed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "breed_id")
    private Long breedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type", length = 10, nullable = false)
    private PetType petType;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Builder.Default
    @Column(name = "is_popular", nullable = false)
    private boolean isPopular = false;
}
