package com.smartswine.repository;

import com.smartswine.model.Payment;
import com.smartswine.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
}
