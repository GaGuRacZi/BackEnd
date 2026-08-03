package com.gaguraczi.paw.domain.region.entity;

import com.gaguraczi.paw.domain.region.enums.RegionLevel;
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
        name = "legal_region",
        indexes = {
                @Index(name = "idx_legal_region_level_abolished", columnList = "level, abolished"),
                @Index(name = "idx_legal_region_parent", columnList = "parent_code"),
                @Index(name = "idx_legal_region_name", columnList = "name")
        }
)
public class LegalRegion extends BaseEntity {

    @Id
    @Column(name = "code", length = 10, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 20, nullable = false)
    private RegionLevel level;

    @Column(name = "parent_code", length = 10)
    private String parentCode;

    /** 원본 폐지여부: true=폐지, false=존재 */
    @Builder.Default
    @Column(name = "abolished", nullable = false)
    private boolean abolished = false;

    public void applySync(String name, RegionLevel level, String parentCode, boolean abolished) {
        this.name = name;
        this.level = level;
        this.parentCode = parentCode;
        this.abolished = abolished;
    }
}
