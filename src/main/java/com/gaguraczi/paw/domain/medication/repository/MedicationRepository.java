package com.gaguraczi.paw.domain.medication.repository;

import com.gaguraczi.paw.domain.medication.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    Optional<Medication> findByItemSeq(String itemSeq);

    boolean existsByItemSeq(String itemSeq);
}
