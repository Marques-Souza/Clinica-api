package com.marquesdev.clinica.dto;

import java.math.BigDecimal;
import java.util.List;

public record AppointmentAvailabilityResponseDto(
        Integer durationMinutes,
        BigDecimal consultationFee,
        List<String> availableSlots
) {
}
