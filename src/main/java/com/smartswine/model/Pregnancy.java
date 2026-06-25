package com.smartswine.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pregnancies")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregnancy {

    public enum Status { CONFIRMED, IN_PROGRESS, COMPLETED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breeding_record_id", nullable = false)
    private BreedingRecord breedingRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sow_id", nullable = false)
    private Pig sow;

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(name = "expected_birth_date")
    private LocalDate expectedBirthDate;

    @Column(name = "actual_birth_date")
    private LocalDate actualBirthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.CONFIRMED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
