package com.smartswine.service;

import com.smartswine.dto.request.PigRequest;
import com.smartswine.model.Building;
import com.smartswine.model.Farm;
import com.smartswine.model.Pig;
import com.smartswine.model.User;
import com.smartswine.model.enums.PigStatus;
import com.smartswine.repository.BuildingRepository;
import com.smartswine.repository.FarmRepository;
import com.smartswine.repository.PigRepository;
import com.smartswine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PigService {

    private final PigRepository pigRepository;
    private final FarmRepository farmRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<Pig> findAll() { return pigRepository.findAll(); }

    public Pig findById(Long id) {
        return pigRepository.findById(id).orElseThrow(() -> new RuntimeException("Pig not found: " + id));
    }

    public List<Pig> findByStatus(PigStatus status) { return pigRepository.findByStatus(status); }

    public List<Pig> findByFarm(Long farmId) { return pigRepository.findByFarmId(farmId); }

    @Transactional
    public Pig create(PigRequest req, Long createdById) {
        if (pigRepository.existsByTagNumber(req.getTagNumber())) {
            throw new RuntimeException("Tag number already exists: " + req.getTagNumber());
        }
        Farm farm = farmRepository.findById(req.getFarmId()).orElseThrow(() -> new RuntimeException("Farm not found"));
        User createdBy = userRepository.findById(createdById).orElse(null);

        Pig pig = Pig.builder()
                .tagNumber(req.getTagNumber())
                .name(req.getName())
                .gender(req.getGender())
                .breed(req.getBreed())
                .birthDate(req.getBirthDate())
                .weightKg(req.getWeightKg())
                .status(req.getStatus() != null ? req.getStatus() : PigStatus.ACTIVE)
                .farm(farm)
                .notes(req.getNotes())
                .createdBy(createdBy)
                .build();

        if (req.getBuildingId() != null) {
            Building building = buildingRepository.findById(req.getBuildingId()).orElse(null);
            pig.setBuilding(building);
        }
        if (req.getParentSowId() != null) {
            pig.setParentSow(pigRepository.findById(req.getParentSowId()).orElse(null));
        }
        if (req.getParentBoarId() != null) {
            pig.setParentBoar(pigRepository.findById(req.getParentBoarId()).orElse(null));
        }

        Pig saved = pigRepository.save(pig);
        auditService.log(createdById, createdBy != null ? createdBy.getUsername() : null,
                "CREATE_PIG", "Pig", saved.getId(), null, saved.getTagNumber(), null, null);
        return saved;
    }

    @Transactional
    public Pig update(Long id, PigRequest req, Long updatedById) {
        Pig pig = findById(id);
        pig.setName(req.getName());
        pig.setBreed(req.getBreed());
        pig.setBirthDate(req.getBirthDate());
        pig.setWeightKg(req.getWeightKg());
        if (req.getStatus() != null) pig.setStatus(req.getStatus());
        if (req.getBuildingId() != null) {
            pig.setBuilding(buildingRepository.findById(req.getBuildingId()).orElse(null));
        }
        pig.setNotes(req.getNotes());
        return pigRepository.save(pig);
    }

    @Transactional
    public void updateStatus(Long id, PigStatus status, Long userId) {
        Pig pig = findById(id);
        String oldStatus = pig.getStatus().name();
        pig.setStatus(status);
        pigRepository.save(pig);
        auditService.log(userId, null, "UPDATE_PIG_STATUS", "Pig", id, oldStatus, status.name(), null, null);
    }

    public Map<String, Long> getStats() {
        return Map.of(
            "total", pigRepository.count(),
            "active", pigRepository.countByStatus(PigStatus.ACTIVE),
            "breeding", pigRepository.countByStatus(PigStatus.BREEDING),
            "pregnant", pigRepository.countByStatus(PigStatus.PREGNANT),
            "available", pigRepository.countByStatus(PigStatus.AVAILABLE),
            "sold", pigRepository.countByStatus(PigStatus.SOLD)
        );
    }
}
