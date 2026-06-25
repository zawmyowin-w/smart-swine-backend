package com.smartswine.dto.request;

import com.smartswine.model.Pig;
import com.smartswine.model.enums.PigStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PigRequest {
    @NotBlank
    private String tagNumber;
    private String name;
    @NotNull
    private Pig.Gender gender;
    private String breed;
    private LocalDate birthDate;
    private BigDecimal weightKg;
    private PigStatus status;
    private Long buildingId;
    @NotNull
    private Long farmId;
    private Long parentSowId;
    private Long parentBoarId;
    private String notes;
}
