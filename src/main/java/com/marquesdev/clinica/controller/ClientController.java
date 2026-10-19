package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.ClientRequestDto;
import com.marquesdev.clinica.dto.ClientResponseDto;
import com.marquesdev.clinica.dto.ClientUpdateRequestDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Clients", description = "Contains all operations related to client profile management for creating, updating and retrieving client information.")
@RestController
@RequestMapping("${app.api.endpoints.clients}")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Create client profile",
            description = "Create a client profile for the authenticated user. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Client profile created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Client profile already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PutMapping("/me")
    public ResponseEntity<ClientResponseDto> createClient(
            @Valid @RequestBody ClientRequestDto clientRequestDto,
            Authentication authentication) {
        String email = authentication.getName();

        ClientResponseDto client = clientService.createClient(
                email, clientRequestDto
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(client);
    }

    @Operation(summary = "Update client profile",
            description = "Update the authenticated user's client profile. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Client profile updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Client profile not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PatchMapping("/me")
    public ResponseEntity<ClientResponseDto> updateClient(
            @Valid @RequestBody ClientUpdateRequestDto clientUpdateRequestDto,
            Authentication authentication) {
        String email = authentication.getName();

        ClientResponseDto client = clientService.updateClient(
                email, clientUpdateRequestDto
        );
        return ResponseEntity.ok(client);
    }


    @Operation(summary = "Retrieve client profile",
            description = "Get the authenticated user's client profile. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Client profile retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Client profile not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ClientResponseDto> getMyClient(
            Authentication authentication) {
        String email = authentication.getName();
        ClientResponseDto client = clientService.getMyClient(email);
        return ResponseEntity.ok(client);
    }


}
