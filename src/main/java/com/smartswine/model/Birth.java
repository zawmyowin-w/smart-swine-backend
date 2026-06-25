package com.smartswine.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "births")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Birth {

    public enum Status { PENDING_HR, HR_CONFIRMED, AVAILABLE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregnancy_id", nullable = false)
    private Pregnancy pregnancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sow_id", nullable = false)
    private Pig sow;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "total_born", nullable = false)
    @Builder.Default
    private Integer totalBorn = 0;

    @Column(name = "born_alive", nullable = false)
    @Builder.Default
    private Integer bornAlive = 0;

    @Column(name = "stillborn", nullable = false)
    @Builder.Default
    private Integer stillborn = 0;

    @Column(name = "buffer_count", nullable = false)
    @Builder.Default
    private Integer bufferCount = 0;

    @Column(name = "available_count", nullable = false)
    @Builder.Default
    private Integer availableCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING_HR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hr_confirmed_by")
    private User hrConfirmedBy;

    @Column(name = "hr_confirmed_at")
    private LocalDateTime hrConfirmedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
