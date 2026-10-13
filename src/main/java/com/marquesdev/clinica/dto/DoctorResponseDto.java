package com.marquesdev.clinica.dto;

import com.marquesdev.clinica.enums.Specialty;

import java.util.UUID;

public record DoctorResponseDto(
        UUID id,
        String fullName,
        String cpf,
        String crm,
        Specialty specialty,
        Boolean active,
        String email

) {
}
