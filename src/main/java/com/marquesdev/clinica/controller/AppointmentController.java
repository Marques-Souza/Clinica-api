package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.AppointmentRequestDto;
import com.marquesdev.clinica.dto.AppointmentResponseDto;
import com.marquesdev.clinica.dto.AppointmentUpdateRequestDto;
import com.marquesdev.clinica.exception.ErrorMessage;
import com.marquesdev.clinica.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Appointments", description = "Contains all operations related to appointment management for creating, updating, listing and retrieving.")
@RestController
@RequestMapping("${app.api.endpoints.appointments}")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(summary = "Create a new appointment",
            description = "Create a new appointment by specifying specialty. An available doctor will be automatically assigned based on the specialty and schedule availability.",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Appointment created successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Schedule conflict - no doctor available for the selected time",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Client, specialty or no active doctors available",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody AppointmentRequestDto appointmentRequestDto,
            Authentication authentication){
        AppointmentResponseDto appointment = appointmentService.createAppointment(
                authentication.getName(),
                appointmentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @Operation(summary = "Retrieve all appointments for authenticated user",
            description = "Endpoint to list all appointments for the authenticated user with pagination support (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AppointmentResponseDto.class)))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Page<AppointmentResponseDto>> findAll(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication){
        Page<AppointmentResponseDto> appointments = appointmentService.findAll(
                authentication.getName(),
                pageable);
        return ResponseEntity.ok(appointments);

    }

    @Operation(summary = "Retrieve an appointment by code",
            description = "Endpoint to get an appointment by its unique code. Accessible by users and admins (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Appointment retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Appointment not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<AppointmentResponseDto> findByCode(@PathVariable String code){
        AppointmentResponseDto appointment = appointmentService.findByCode(code);
        return ResponseEntity.ok(appointment);
    }

    @Operation(summary = "Update an appointment",
            description = "Update an appointment's scheduled time, reason, or status. Users can only update their own appointments. When status is set to CANCELLED, payment status is automatically set to CANCELLED as well. (Bearer Token required)",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Appointment updated successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - user can only update their own appointments",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Appointment not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Invalid fields",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AppointmentResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentUpdateRequestDto appointmentUpdateRequestDto,
            Authentication authentication){
        AppointmentResponseDto appointment = appointmentService.updateAppointment(
                authentication.getName(),
                id,
                appointmentUpdateRequestDto
        );
        return ResponseEntity.ok(appointment);
    }
}
