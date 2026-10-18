package com.marquesdev.clinica.controller;

import com.marquesdev.clinica.dto.SpecialtyConfigRequestDto;
import com.marquesdev.clinica.dto.SpecialtyConfigResponseDto;
import com.marquesdev.clinica.dto.SpecialtyConfigUpdateRequestDto;
import com.marquesdev.clinica.enums.Specialty;
import com.marquesdev.clinica.service.SpecialtyConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.api.endpoints.specialties}")
@RequiredArgsConstructor
public class SpecialtyConfigController {

    private final SpecialtyConfigService specialtyConfigService;


    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyConfigResponseDto> createSpecialtyConfig(@Valid @RequestBody SpecialtyConfigRequestDto specialtyConfigRequestDto){
        SpecialtyConfigResponseDto specialtyConfig = specialtyConfigService.createSpecialtyConfig(specialtyConfigRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtyConfig);
    }

    @GetMapping("/{specialty}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SpecialtyConfigResponseDto> findBySpecialty(@PathVariable Specialty specialty){
        SpecialtyConfigResponseDto specialtyConfig = specialtyConfigService.findBySpecialty(specialty);
        return ResponseEntity.ok(specialtyConfig);
    }

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
