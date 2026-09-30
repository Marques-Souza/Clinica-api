package com.marquesdev.clinica.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String email,
        String role,
        String url
) {
}
