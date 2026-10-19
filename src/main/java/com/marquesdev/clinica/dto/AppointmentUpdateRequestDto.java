package com.marquesdev.clinica.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marquesdev.clinica.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentUpdateRequestDto(

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Future(message = "Scheduled date must be in the future.")
        LocalDateTime scheduledAt,

        AppointmentStatus status,

        @Size(max = 500)
        String reason
) {
}
