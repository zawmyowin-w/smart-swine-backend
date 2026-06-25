package com.smartswine.repository;

import com.smartswine.model.Birth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BirthRepository extends JpaRepository<Birth, Long> {
    List<Birth> findByStatus(Birth.Status status);
    List<Birth> findBySowId(Long sowId);
    List<Birth> findByStatusOrderByCreatedAtDesc(Birth.Status status);
}
