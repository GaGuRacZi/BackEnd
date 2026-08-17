package com.gaguraczi.paw.domain.walkcourse.entity;

import com.gaguraczi.paw.domain.users.entity.Pet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(
        name = "walk_course",
        indexes = {
                @Index(name = "idx_course_pet_use", columnList = "pet_id, use_count")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;


    @Column(name = "name", nullable = false, length = 50)
    private String name;


    @Column(name = "distance", nullable = false, precision = 4, scale = 1)
    private BigDecimal distance;


    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;


    @Column(name = "path_json", columnDefinition = "TEXT")
    private String pathJson;


    @Column(name = "use_count", nullable = false)
    private Integer useCount;


    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Builder
    private WalkCourseEntity(Pet pet, String name, BigDecimal distance,
                       String thumbnailUrl, String pathJson) {
        this.pet = pet;
        this.name = name;
        this.distance = distance;
        this.thumbnailUrl = thumbnailUrl;
        this.pathJson = pathJson;
        this.useCount = 0;
        this.lastUsedAt = null;
    }


    public void markUsed(LocalDateTime usedAt) {
        this.useCount = this.useCount + 1;
        this.lastUsedAt = usedAt;
    }


    public void update(String name, BigDecimal distance, String thumbnailUrl, String pathJson) {
        if (name != null) this.name = name;
        if (distance != null) this.distance = distance;
        if (thumbnailUrl != null) this.thumbnailUrl = thumbnailUrl;
        if (pathJson != null) this.pathJson = pathJson;
    }


    public boolean isOwnedBy(Long petId) {
        return this.pet.getPetId().equals(petId);
    }
}
