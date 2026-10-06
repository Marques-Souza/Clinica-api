package com.marquesdev.clinica.dto;

import java.util.UUID;

public record ClientResponseDto(
        UUID id,
        String fullName,
        String cpf,
        String phone,
        String email,
        String cep,
        String street,
        String neighborhood,
        String city,
        String state,
        String number

) {
}
