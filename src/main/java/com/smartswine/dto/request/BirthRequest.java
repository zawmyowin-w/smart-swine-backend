package com.smartswine.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BirthRequest {
    @NotNull
    private Long pregnancyId;
    @NotNull
    private LocalDate birthDate;
    @NotNull
    @Min(0)
    private Integer totalBorn;
    @NotNull
    @Min(0)
    private Integer bornAlive;
    @Min(0)
    private Integer stillborn = 0;
    private String notes;
}
