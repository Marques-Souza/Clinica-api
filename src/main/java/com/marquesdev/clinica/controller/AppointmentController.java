package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.AppointmentRequestDto;
import com.marquesdev.clinica.dto.AppointmentResponseDto;
import com.marquesdev.clinica.dto.AppointmentUpdateRequestDto;
import com.marquesdev.clinica.service.AppointmentService;
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

@RestController
@RequestMapping("${app.api.endpoints.appointments}")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

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

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<AppointmentResponseDto> findByCode(@PathVariable String code){
        AppointmentResponseDto appointment = appointmentService.findByCode(code);
        return ResponseEntity.ok(appointment);
    }

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
