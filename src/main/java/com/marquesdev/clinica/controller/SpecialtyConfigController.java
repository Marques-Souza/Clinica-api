package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.SpecialtyConfigRequestDto;
import com.marquesdev.clinica.dto.SpecialtyConfigResponseDto;
import com.marquesdev.clinica.dto.SpecialtyConfigUpdateRequestDto;
import com.marquesdev.clinica.enums.Specialty;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.service.SpecialtyConfigService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Specialty Configurations", description = "Contains all operations related to specialty configuration management for setting consultation fees and duration.")
@RestController
@RequestMapping("${app.api.endpoints.specialties}")
@RequiredArgsConstructor
public class SpecialtyConfigController {

    private final SpecialtyConfigService specialtyConfigService;


    @Operation(summary = "Create a new specialty configuration",
            description = "Create a new specialty configuration with consultation fee and duration. Only admins can create specialty configurations. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Specialty configuration created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpecialtyConfigResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only admins can create specialty configurations",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Specialty configuration already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyConfigResponseDto> createSpecialtyConfig(@Valid @RequestBody SpecialtyConfigRequestDto specialtyConfigRequestDto){
        SpecialtyConfigResponseDto specialtyConfig = specialtyConfigService.createSpecialtyConfig(specialtyConfigRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtyConfig);
    }

    @Operation(summary = "Retrieve a specialty configuration by specialty",
            description = "Endpoint to get a specialty configuration by its specialty type. Only admins can access. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Specialty configuration retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpecialtyConfigResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only admins can access specialty configurations",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Specialty configuration not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/{specialty}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyConfigResponseDto> findBySpecialty(@PathVariable Specialty specialty){
        SpecialtyConfigResponseDto specialtyConfig = specialtyConfigService.findBySpecialty(specialty);
        return ResponseEntity.ok(specialtyConfig);
    }

    @Operation(summary = "Update a specialty configuration",
            description = "Update an existing specialty configuration's consultation fee and duration. Only admins can update. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Specialty configuration updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SpecialtyConfigResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only admins can update specialty configurations",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Specialty configuration not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PutMapping("/{specialty}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyConfigResponseDto> updateSpecialtyConfig(
            @PathVariable Specialty specialty,
            @Valid @RequestBody SpecialtyConfigUpdateRequestDto specialtyConfigUpdateRequestDto){
        SpecialtyConfigResponseDto specialtyConfig = specialtyConfigService.updateSpecialtyConfig(
                specialty, specialtyConfigUpdateRequestDto);
        return ResponseEntity.ok(specialtyConfig);
    }





}
