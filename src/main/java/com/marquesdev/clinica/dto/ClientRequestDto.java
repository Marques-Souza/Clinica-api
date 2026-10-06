package com.marquesdev.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record ClientRequestDto(

        @NotBlank(message = "Full name cannot be blank.")
        @Size(max = 150, message = "full name must not exceed 150 characters.")
        String fullName,

        @NotBlank(message = "Cpf cannot be blank.")
        @Pattern(regexp = "\\d{11}", message = "Invalid CPF format. It should contain exactly 11 digits.")
        @CPF(message = "Invalid CPF format.")
        String cpf,

        @NotBlank(message = "Phone cannot be blank.")
        @Size(max = 20, message = "phone must not exceed 20 characters.")
        String phone,

        @NotBlank(message = "Cep cannot be blank.")
        @Pattern(regexp = "\\d{8}", message = "Invalid CEP format. It should contain exactly 8 digits.")
        String cep,

        @NotBlank(message = "Number cannot be blank.")
        @Size(max = 10, message = "number must not exceed 10 characters.")
        String number

) {
}
