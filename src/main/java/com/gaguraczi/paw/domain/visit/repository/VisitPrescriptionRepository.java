package com.gaguraczi.paw.domain.visit.repository;

import com.gaguraczi.paw.domain.visit.entity.VisitPrescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitPrescriptionRepository extends JpaRepository<VisitPrescription, Long> {

    Optional<VisitPrescription> findByPrescriptionIdAndVisit_VisitId(Long prescriptionId, Long visitId);
}
