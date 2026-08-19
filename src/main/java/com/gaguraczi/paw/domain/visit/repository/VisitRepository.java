package com.gaguraczi.paw.domain.visit.repository;

import com.gaguraczi.paw.domain.visit.entity.Visit;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByPet_PetIdAndUser_UidOrderByCreatedAtDesc(Long petId, UUID uid);

    @EntityGraph(attributePaths = {
            "pet",
            "pet.breed",
            "pet.user",
            "user",
            "prescriptions",
            "prescriptions.medication"
    })
    Optional<Visit> findByVisitIdAndUser_Uid(Long visitId, UUID uid);

    @Query("SELECT DISTINCT v FROM Visit v LEFT JOIN FETCH v.transcriptTurns WHERE v.visitId = :visitId")
    Optional<Visit> findByVisitIdWithTranscriptTurns(@Param("visitId") Long visitId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Visit v WHERE v.visitId = :visitId")
    Optional<Visit> findByIdForUpdate(@Param("visitId") Long visitId);
}
