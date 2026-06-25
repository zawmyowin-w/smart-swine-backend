package com.smartswine.dto.request;

import com.smartswine.model.BreedingRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BreedingRequest {
    @NotNull
    private Long sowId;
    @NotNull
    private Long boarId;
    @NotNull
    private LocalDate breedingDate;
    private BreedingRecord.Method method = BreedingRecord.Method.NATURAL;
    private String notes;
}
