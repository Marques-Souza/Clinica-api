package com.marquesdev.clinica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordRecoveryConfirmDto(

        @NotBlank(message = "Email cannot be blank.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "Recovery code cannot be blank.")
        @Pattern(regexp = "\\d{6}", message = "Recovery code must contain six digits.")
        String code,

        @NotBlank(message = "New password cannot be blank.")
        @Size(min = 5, max = 12, message = "New password must be between 5 and 12 characters.")
        String newPassword,

        @NotBlank(message = "Confirm password cannot be blank.")
        @Size(min = 5, max = 12, message = "Confirm password must be between 5 and 12 characters.")
        String confirmPassword
) {
}
