package com.marquesdev.clinica.dto;

import com.marquesdev.clinica.enums.Specialty;

import java.util.UUID;

public record DoctorDto(
        UUID id,
        String fullName,
        Specialty specialty
) {
}
