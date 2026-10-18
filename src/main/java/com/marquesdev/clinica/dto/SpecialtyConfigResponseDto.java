package com.marquesdev.clinica.dto;

import com.marquesdev.clinica.enums.Specialty;

import java.math.BigDecimal;
import java.util.UUID;

public record SpecialtyConfigResponseDto(

        UUID id,
        Specialty specialty,
        BigDecimal consultationFee,
        Integer durationMinutes
) {
}
