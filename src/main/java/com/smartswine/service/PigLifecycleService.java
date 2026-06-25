package com.smartswine.service;

import com.smartswine.dto.request.BirthRequest;
import com.smartswine.dto.request.BreedingRequest;
import com.smartswine.model.*;
import com.smartswine.model.enums.PigStatus;
import com.smartswine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PigLifecycleService {

    private static final int BUFFER_COUNT = 2;

    private final BreedingRecordRepository breedingRecordRepository;
    private final PregnancyRepository pregnancyRepository;
    private final BirthRepository birthRepository;
    private final PigRepository pigRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public BreedingRecord recordBreeding(BreedingRequest req, Long supervisorId) {
        Pig sow = pigRepository.findById(req.getSowId()).orElseThrow(() -> new RuntimeException("Sow not found"));
        Pig boar = pigRepository.findById(req.getBoarId()).orElseThrow(() -> new RuntimeException("Boar not found"));
        User supervisor = userRepository.findById(supervisorId).orElse(null);

        BreedingRecord record = BreedingRecord.builder()
                .sow(sow).boar(boar)
                .breedingDate(req.getBreedingDate())
                .method(req.getMethod())
                .status(BreedingRecord.Status.PENDING)
                .notes(req.getNotes())
                .recordedBy(supervisor)
                .build();

        sow.setStatus(PigStatus.BREEDING);
        pigRepository.save(sow);

        BreedingRecord saved = breedingRecordRepository.save(record);
        auditService.log(supervisorId, null, "RECORD_BREEDING", "BreedingRecord", saved.getId(), null, null, null, null);
        return saved;
    }

    @Transactional
    public Pregnancy confirmPregnancy(Long breedingRecordId, Long userId) {
        BreedingRecord record = breedingRecordRepository.findById(breedingRecordId)
                .orElseThrow(() -> new RuntimeException("Breeding record not found"));
        User confirmedBy = userRepository.findById(userId).orElse(null);

        record.setStatus(BreedingRecord.Status.CONFIRMED);
        breedingRecordRepository.save(record);

        LocalDate confirmedDate = LocalDate.now();
        LocalDate expectedBirth = confirmedDate.plusDays(114); // Pig gestation ~114 days

        Pregnancy pregnancy = Pregnancy.builder()
                .breedingRecord(record)
                .sow(record.getSow())
                .confirmedDate(confirmedDate)
                .expectedBirthDate(expectedBirth)
                .status(Pregnancy.Status.CONFIRMED)
                .confirmedBy(confirmedBy)
                .build();

        record.getSow().setStatus(PigStatus.PREGNANT);
        pigRepository.save(record.getSow());

        Pregnancy saved = pregnancyRepository.save(pregnancy);
        auditService.log(userId, null, "CONFIRM_PREGNANCY", "Pregnancy", saved.getId(), null, null, null, null);
        return saved;
    }

    @Transactional
    public Birth recordBirth(BirthRequest req, Long supervisorId) {
        Pregnancy pregnancy = pregnancyRepository.findById(req.getPregnancyId())
                .orElseThrow(() -> new RuntimeException("Pregnancy not found"));
        User supervisor = userRepository.findById(supervisorId).orElse(null);

        int bufferCount = Math.min(BUFFER_COUNT, req.getBornAlive());
        int available = req.getBornAlive() - bufferCount;

        Birth birth = Birth.builder()
                .pregnancy(pregnancy)
                .sow(pregnancy.getSow())
                .birthDate(req.getBirthDate())
                .totalBorn(req.getTotalBorn())
                .bornAlive(req.getBornAlive())
                .stillborn(req.getStillborn())
                .bufferCount(bufferCount)
                .availableCount(available)
                .status(Birth.Status.PENDING_HR)
                .supervisor(supervisor)
                .notes(req.getNotes())
                .build();

        pregnancy.setStatus(Pregnancy.Status.COMPLETED);
        pregnancy.setActualBirthDate(req.getBirthDate());
        pregnancyRepository.save(pregnancy);

        pregnancy.getSow().setStatus(PigStatus.ACTIVE);
        pigRepository.save(pregnancy.getSow());

        Birth saved = birthRepository.save(birth);
        auditService.log(supervisorId, null, "RECORD_BIRTH", "Birth", saved.getId(), null,
                "BornAlive:" + req.getBornAlive(), null, null);
        return saved;
    }

    @Transactional
    public Birth hrConfirmBirth(Long birthId, Long hrUserId) {
        Birth birth = birthRepository.findById(birthId)
                .orElseThrow(() -> new RuntimeException("Birth not found"));
        User hrUser = userRepository.findById(hrUserId).orElse(null);

        birth.setStatus(Birth.Status.HR_CONFIRMED);
        birth.setHrConfirmedBy(hrUser);
        birth.setHrConfirmedAt(LocalDateTime.now());

        Birth saved = birthRepository.save(birth);
        auditService.log(hrUserId, hrUser != null ? hrUser.getUsername() : null,
                "HR_CONFIRM_BIRTH", "Birth", birthId, null, "CONFIRMED", null, null);
        return saved;
    }

    @Transactional
    public Birth markBirthAvailable(Long birthId, Long userId) {
        Birth birth = birthRepository.findById(birthId)
                .orElseThrow(() -> new RuntimeException("Birth not found"));
        birth.setStatus(Birth.Status.AVAILABLE);
        return birthRepository.save(birth);
    }

    public List<Birth> getPendingHrConfirmations() {
        return birthRepository.findByStatusOrderByCreatedAtDesc(Birth.Status.PENDING_HR);
    }

    public List<Pregnancy> getActivePregnancies() {
        return pregnancyRepository.findByStatusOrderByExpectedBirthDateAsc(Pregnancy.Status.CONFIRMED);
    }
}
