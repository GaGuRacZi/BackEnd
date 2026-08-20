package com.gaguraczi.paw.domain.breed.repository;

import com.gaguraczi.paw.domain.breed.entity.Breed;
import com.gaguraczi.paw.domain.users.enums.PetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BreedRepository extends JpaRepository<Breed, Long> {

    Optional<Breed> findByBreedIdAndPetType(Long breedId, PetType petType);

    Optional<Breed> findByPetTypeAndName(PetType petType, String name);

    List<Breed> findByPetTypeAndIsPopularTrueOrderByNameAsc(PetType petType);

    @Query("""
            SELECT b FROM Breed b
            WHERE b.petType = :petType
              AND (:#{#q == null || #q.isEmpty()} = true OR LOWER(b.name) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
            ORDER BY b.name ASC
            """)
    List<Breed> search(@Param("petType") PetType petType, @Param("q") String q);

    @Query("""
            SELECT b FROM Breed b
            WHERE b.petType = :petType
              AND b.isPopular = true
              AND (:#{#q == null || #q.isEmpty()} = true OR LOWER(b.name) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
            ORDER BY b.name ASC
            """)
    List<Breed> searchPopular(@Param("petType") PetType petType, @Param("q") String q);
}
