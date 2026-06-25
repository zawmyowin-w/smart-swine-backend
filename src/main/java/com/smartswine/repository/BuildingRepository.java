package com.smartswine.repository;

import com.smartswine.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    List<Building> findByFarmId(Long farmId);
    List<Building> findByFarmIdAndIsActiveTrue(Long farmId);
    List<Building> findByType(Building.BuildingType type);
}
