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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pet_registration")
public class PetRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_registration_id")
    private Long petRegistrationId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false, unique = true)
    private Pet pet;

    @Column(name = "guardian_name", length = 50, nullable = false)
    private String guardianName;

    @Column(name = "registration_number", length = 50, nullable = false)
    private String registrationNumber;

    @Column(name = "photo_s3_key", length = 255, unique = true)
    private String photoS3Key;

    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    public void update(String guardianName, String registrationNumber) {
        if (guardianName != null && !guardianName.isBlank()) {
            this.guardianName = guardianName.trim();
        }
        if (registrationNumber != null && !registrationNumber.isBlank()) {
            this.registrationNumber = registrationNumber.trim();
        }
    }

    public void updatePhoto(String photoS3Key, String photoUrl) {
        this.photoS3Key = photoS3Key;
        this.photoUrl = photoUrl;
    }
}
