package com.marquesdev.clinica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordRecoveryRequestDto(

        @NotBlank(message = "Email cannot be blank.")
        @Email(message = "Invalid email format.")
        String email
) {
}
