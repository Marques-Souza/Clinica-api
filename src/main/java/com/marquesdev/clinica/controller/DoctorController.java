package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.DoctorRequestDto;
import com.marquesdev.clinica.dto.DoctorResponseDto;
import com.marquesdev.clinica.dto.DoctorUpdateRequestDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.service.DoctorService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Doctors", description = "Contains all operations related to doctor profile management for creating, updating and retrieving doctor information.")
@RestController
@RequestMapping("${app.api.endpoints.doctors}")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @Operation(summary = "Create doctor profile",
            description = "Create a doctor profile for the authenticated user. Only users with DOCTOR role can create their profile. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Doctor profile created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only doctors can create their profile",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Doctor profile already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<DoctorResponseDto> createDoctor(@Valid @RequestBody DoctorRequestDto doctorRequestDto, Authentication authentication){
        String email = authentication.getName();
        DoctorResponseDto doctor = doctorService.createDoctor(email, doctorRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
    }

    @Operation(summary = "Retrieve doctor profile",
            description = "Get the authenticated doctor's profile. Only users with DOCTOR role can access. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Doctor profile retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only doctors can access their profile",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Doctor profile not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<DoctorResponseDto> getMyDoctor(Authentication authentication){
        String email = authentication.getName();
        DoctorResponseDto doctor = doctorService.getMyDoctor(email);
        return ResponseEntity.ok(doctor);
    }

    @Operation(summary = "Retrieve doctor by ID",
            description = "Get a doctor's profile by ID. Only admins can access. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Doctor profile retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only admins can access doctor profiles by ID",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Doctor profile not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorResponseDto> getDoctorById(@PathVariable UUID id){
        DoctorResponseDto doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @Operation(summary = "Update doctor profile",
            description = "Update a doctor's profile by ID. Only admins can update doctor profiles. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Doctor profile updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - only admins can update doctor profiles",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Doctor profile not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorResponseDto> updateDoctorById(@PathVariable UUID id, @Valid @RequestBody DoctorUpdateRequestDto doctorUpdateRequestDto){
        DoctorResponseDto doctor = doctorService.updateDoctorById(id, doctorUpdateRequestDto);
        return ResponseEntity.ok(doctor);
    }

}
