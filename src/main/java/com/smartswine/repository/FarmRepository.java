package com.smartswine.repository;

import com.smartswine.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByIsActiveTrue();
    List<Farm> findByManagerId(Long managerId);
}
