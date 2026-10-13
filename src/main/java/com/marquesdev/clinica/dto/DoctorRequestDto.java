package com.marquesdev.clinica.dto;

import com.marquesdev.clinica.enums.Specialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record DoctorRequestDto(

        @NotBlank(message = "Full name cannot be blank.")
        @Size(max = 150, message = "full name must not exceed 150 characters.")
        String fullName,

        @NotBlank(message = "Cpf cannot be blank.")
        @Pattern(regexp = "\\d{11}", message = "Invalid CPF format. It should contain exactly 11 digits.")
        @CPF(message = "Invalid CPF format.")
        String cpf,

        @NotBlank(message = "Crm cannot be blank.")
        @Pattern(regexp = "\\d{6}-[A-Z]{2}", message = "Invalid CRM format. Expected format: 123456-SP")
        @Size(min = 9, max = 9, message = "CRM must be exactly 9 characters.")
        String crm,

        @NotNull(message = "Specialty cannot be null.")
        Specialty specialty

) {
}
