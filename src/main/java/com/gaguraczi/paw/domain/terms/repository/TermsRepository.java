package com.gaguraczi.paw.domain.terms.repository;

import com.gaguraczi.paw.domain.terms.entity.Terms;
import com.gaguraczi.paw.domain.terms.enums.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findAllByOrderByRequiredDescTypeAsc();

    Optional<Terms> findFirstByTypeOrderByEffectiveAtDesc(TermsType type);

    boolean existsByTypeAndVersion(TermsType type, String version);
}
