package com.marquesdev.clinica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginDto(

        @NotBlank(message = "Email cannot be blank.")
        @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format.")
        String email,

        @NotBlank(message = "Password cannot be blank.")
        String password

) {
}
