package com.marquesdev.clinica.integration.viacep;

public record ViaCepResponseDto(
        String logradouro,
        String bairro,
        String localidade,
        String uf
) {
}
