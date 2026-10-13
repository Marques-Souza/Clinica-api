package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.DoctorRequestDto;
import com.marquesdev.clinica.dto.DoctorResponseDto;
import com.marquesdev.clinica.dto.DoctorUpdateRequestDto;
import com.marquesdev.clinica.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${app.api.endpoints.doctors}")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<DoctorResponseDto> createDoctor(@Valid @RequestBody DoctorRequestDto doctorRequestDto, Authentication authentication){
        String email = authentication.getName();
        DoctorResponseDto doctor = doctorService.createDoctor(email, doctorRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(doctor);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<DoctorResponseDto> getMyDoctor(Authentication authentication){
        String email = authentication.getName();
        DoctorResponseDto doctor = doctorService.getMyDoctor(email);
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorResponseDto> getDoctorById(@PathVariable UUID id){
        DoctorResponseDto doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DoctorResponseDto> updateDoctorById(@PathVariable UUID id, @Valid @RequestBody DoctorUpdateRequestDto doctorUpdateRequestDto){
        DoctorResponseDto doctor = doctorService.updateDoctorById(id, doctorUpdateRequestDto);
        return ResponseEntity.ok(doctor);
    }

}
