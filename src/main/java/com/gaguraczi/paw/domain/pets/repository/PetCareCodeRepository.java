package com.gaguraczi.paw.domain.pets.repository;

import com.gaguraczi.paw.domain.pets.entity.PetCareCode;
import com.gaguraczi.paw.domain.pets.enums.PetCareCategory;
import com.gaguraczi.paw.domain.users.enums.PetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PetCareCodeRepository extends JpaRepository<PetCareCode, Long> {

    @Query("""
            select c from PetCareCode c
            where c.category = :category
              and (c.species is null or c.species = :species)
              and (:keyword is null or c.name like concat('%', :keyword, '%'))
            order by c.name asc
            """)
    List<PetCareCode> search(
            @Param("category") PetCareCategory category,
            @Param("species") PetType species,
            @Param("keyword") String keyword
    );

    boolean existsByCategory(PetCareCategory category);
}
