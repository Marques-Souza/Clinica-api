package com.marquesdev.clinica.dto;

import com.marquesdev.clinica.enums.Specialty;

public record DoctorUpdateRequestDto(

        Specialty specialty,
        Boolean active
) {
}
