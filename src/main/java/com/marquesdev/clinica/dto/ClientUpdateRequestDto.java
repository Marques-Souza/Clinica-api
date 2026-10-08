package com.marquesdev.clinica.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record ClientUpdateRequestDto(

        @Size(max = 150, message = "full name must not exceed 150 characters.") String fullName,

        @Pattern(regexp = "\\d{11}", message = "Invalid CPF format. It should contain exactly 11 digits.") @CPF(message = "Invalid CPF format.") String cpf,

        @Size(max = 20, message = "phone must not exceed 20 characters.") String phone,

        @Pattern(regexp = "\\d{8}", message = "Invalid CEP format. It should contain exactly 8 digits.") String cep,

        @Size(max = 10, message = "number must not exceed 10 characters.") String number) {
}
