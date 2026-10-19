package com.marquesdev.clinica.dto;

import java.util.UUID;

public record PatientDto(
        UUID id,
        String fullName
) {
}
