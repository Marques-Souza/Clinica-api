package com.marquesdev.clinica.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marquesdev.clinica.enums.AppointmentStatus;
import com.marquesdev.clinica.enums.PaymentMethod;
import com.marquesdev.clinica.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponseDto(

        UUID id,
        String appointmentCode,
        PatientDto patient,
        DoctorDto doctor,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime scheduledAt,
        Integer durationMinutes,
        BigDecimal consultationFee,

        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        AppointmentStatus status,

        String reason
) {
}
