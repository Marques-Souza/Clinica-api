package com.marquesdev.clinica.dto;

import com.marquesdev.clinica.enums.Specialty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SpecialtyConfigRequestDto(

        @NotNull(message = "Specialty cannot be null")
        Specialty specialty,

        @NotNull(message = "Consultation fee cannot be null")
        @DecimalMin(value = "0.01", message = "Consultation fee must be greater than zero.")
        BigDecimal consultationFee,

        @NotNull(message = "Duration minutes cannot be null")
        @Positive(message = "Duration must be greater than zero.")
        Integer durationMinutes
) {
}
