package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.ClientRequestDto;
import com.marquesdev.clinica.dto.ClientResponseDto;
import com.marquesdev.clinica.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.api.endpoints.clients}")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PutMapping("/me")
    public ResponseEntity<ClientResponseDto> createClient(
            @Valid @RequestBody ClientRequestDto clientRequestDto,
            Authentication authentication){
        String email = authentication.getName();

        ClientResponseDto client = clientService.createClient(
                email, clientRequestDto
        );
        return ResponseEntity.ok(client);
    }

    @GetMapping("/me")
    public ResponseEntity<ClientResponseDto> getMyClients(
            Authentication authentication){
        String email = authentication.getName();
        ClientResponseDto client = clientService.getMyClients(email);
        return ResponseEntity.ok(client);
    }



}
