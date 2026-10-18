package com.marquesdev.clinica.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SpecialtyConfigUpdateRequestDto(


        @DecimalMin(value = "0.01", message = "Consultation fee must be greater than zero.")
        BigDecimal consultationFee,


        @Positive(message = "Duration must be greater than zero.")
        Integer durationMinutes
) {
}
