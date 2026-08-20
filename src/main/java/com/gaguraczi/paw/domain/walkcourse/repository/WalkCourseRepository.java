package com.gaguraczi.paw.domain.walkcourse.repository;


import com.gaguraczi.paw.domain.walkcourse.entity.WalkCourseEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface WalkCourseRepository extends JpaRepository<WalkCourseEntity, Long> {


    List<WalkCourseEntity> findAllByPet_PetIdOrderByLastUsedAtDescIdDesc(Long petId);


    List<WalkCourseEntity> findAllByPet_PetIdOrderByUseCountDescLastUsedAtDescIdDesc(Long petId, Limit limit);

    Optional<WalkCourseEntity> findByIdAndPet_PetId(Long courseId, Long petId);

    boolean existsByPet_PetIdAndName(Long petId, String name);

    List<WalkCourseEntity> findAllByPet_PetIdOrderByLastUsedAtDescIdDesc(Long petId, Limit limit);
}
