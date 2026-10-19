package com.marquesdev.clinica.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.marquesdev.clinica.enums.PaymentMethod;
import com.marquesdev.clinica.enums.Specialty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequestDto(

        @NotNull(message = "Specialty cannot be null.")
        Specialty specialty,

        @NotNull(message = "Scheduled at cannot be null.")
        @Future(message = "Scheduled date must be in the future.")
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime scheduledAt,

        @NotNull(message = "Payment method cannot be null.")
        PaymentMethod paymentMethod,

        @Size(max = 500, message = "Reason must not exceed 500 characters.")
        String reason

) {
}
