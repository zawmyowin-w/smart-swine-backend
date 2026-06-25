package com.smartswine.repository;

import com.smartswine.model.Pig;
import com.smartswine.model.enums.PigStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PigRepository extends JpaRepository<Pig, Long> {
    Optional<Pig> findByTagNumber(String tagNumber);
    boolean existsByTagNumber(String tagNumber);
    List<Pig> findByStatus(PigStatus status);
    List<Pig> findByFarmId(Long farmId);
    List<Pig> findByBuildingId(Long buildingId);
    List<Pig> findByFarmIdAndStatus(Long farmId, PigStatus status);
    List<Pig> findByGender(Pig.Gender gender);

    @Query("SELECT COUNT(p) FROM Pig p WHERE p.status = :status")
    long countByStatus(PigStatus status);

    @Query("SELECT COUNT(p) FROM Pig p WHERE p.farm.id = :farmId")
    long countByFarmId(Long farmId);
}
