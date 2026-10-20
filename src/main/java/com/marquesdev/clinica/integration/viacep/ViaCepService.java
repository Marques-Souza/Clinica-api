package com.marquesdev.clinica.integration.viacep;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class ViaCepService {

    private final RestClient restClient;

    public ViaCepService(){
        this.restClient = RestClient.builder()
                .baseUrl("https://viacep.com.br")
                .build();
    }

    public ViaCepResponseDto findAddressByCep(String cep){
        try {
            return restClient
                    .get()
                    .uri("/ws/{cep}/json", cep)
                    .retrieve()
                    .body(ViaCepResponseDto.class);
        } catch (Exception e) {
            log.warn("ViaCEP indisponível para CEP: {}", cep);
            return null;
        }
    }

}
