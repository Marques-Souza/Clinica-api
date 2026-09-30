package com.marquesdev.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRequestDto(

        @NotBlank(message = "Current password cannot be blank.")
        @Size(min = 5, max = 12, message = "Current password must be between 5 and 12 characters.")
        String currentPassword,

        @NotBlank(message = "New password cannot be blank.")
        @Size(min = 5, max = 12, message = "New password must be between 5 and 12 characters.")
        String newPassword,

        @NotBlank(message = "Confirm password cannot be blank.")
        @Size(min = 5, max = 12, message = "Confirm password must be between 5 and 12 characters.")
        String confirmPassword
) {
}
