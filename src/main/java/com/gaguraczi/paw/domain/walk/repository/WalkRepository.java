package com.gaguraczi.paw.domain.walk.repository;

import com.gaguraczi.paw.domain.walk.entity.WalkEntity;
import com.gaguraczi.paw.domain.walk.enums.WalkStatusEnum;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

//산책 기록 DB 접근
public interface WalkRepository extends JpaRepository<WalkEntity, Long> {


    //특정 반려동물의 특정 날짜 기록
    @EntityGraph(attributePaths = {"walkCourse"})
    List<WalkEntity> findAllByPet_PetIdAndWalkDateOrderByStartTimeDesc(Long petId, LocalDate walkDate);

   //특정 반려동물의 특정 기간 기록
    @EntityGraph(attributePaths = {"walkCourse"})
    List<WalkEntity> findAllByPet_PetIdAndWalkDateBetweenOrderByWalkDateDescStartTimeDesc(
            Long petId, LocalDate startDate, LocalDate endDate);

    //특정 반려동물의 전체 기록
    @EntityGraph(attributePaths = {"walkCourse"})
    List<WalkEntity> findAllByPet_PetIdOrderByWalkDateDescStartTimeDesc(Long petId);

    Optional<WalkEntity> findFirstByPet_PetIdAndWalkStatusOrderByStartTimeDesc(Long petId, WalkStatusEnum walkStatus);


    boolean existsByPet_PetIdAndWalkStatus(Long petId, WalkStatusEnum walkStatus);


    Optional<WalkEntity> findByIdAndPet_PetId(Long walkId, Long petId);
}
