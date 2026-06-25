package com.smartswine.repository;

import com.smartswine.model.BreedingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreedingRecordRepository extends JpaRepository<BreedingRecord, Long> {
    List<BreedingRecord> findBySowId(Long sowId);
    List<BreedingRecord> findByStatus(BreedingRecord.Status status);
    List<BreedingRecord> findBySowIdOrderByBreedingDateDesc(Long sowId);
}
