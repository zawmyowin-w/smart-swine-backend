package com.smartswine.repository;

import com.smartswine.model.Pregnancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PregnancyRepository extends JpaRepository<Pregnancy, Long> {
    List<Pregnancy> findBySowId(Long sowId);
    List<Pregnancy> findByStatus(Pregnancy.Status status);
    List<Pregnancy> findByStatusOrderByExpectedBirthDateAsc(Pregnancy.Status status);
}
