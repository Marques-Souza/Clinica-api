package com.marquesdev.clinica.service;

import com.marquesdev.clinica.dto.SpecialtyConfigRequestDto;
import com.marquesdev.clinica.dto.SpecialtyConfigResponseDto;
import com.marquesdev.clinica.dto.SpecialtyConfigUpdateRequestDto;
import com.marquesdev.clinica.entity.SpecialtyConfig;
import com.marquesdev.clinica.enums.Specialty;
import com.marquesdev.clinica.exception.EntityNotFoundException;
import com.marquesdev.clinica.exception.SpecialtyConfigAlreadyExistsException;
import com.marquesdev.clinica.mapper.SpecialtyConfigMapper;
import com.marquesdev.clinica.repository.SpecialtyConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpecialtyConfigService {

    private final SpecialtyConfigRepository specialtyConfigRepository;
    private final SpecialtyConfigMapper specialtyConfigMapper;

    @Transactional
    public SpecialtyConfigResponseDto createSpecialtyConfig(SpecialtyConfigRequestDto specialtyConfigRequestDto){
        if (specialtyConfigRepository.existsBySpecialty(specialtyConfigRequestDto.specialty())) {
            throw new SpecialtyConfigAlreadyExistsException("Specialty config already exists.");
        }
        SpecialtyConfig specialtyConfig = specialtyConfigMapper.toEntity(specialtyConfigRequestDto);
        SpecialtyConfig savedSpecialtyConfig = specialtyConfigRepository.save(specialtyConfig);
        return specialtyConfigMapper.toDto(savedSpecialtyConfig);
    }

    @Transactional(readOnly = true)
    public SpecialtyConfigResponseDto findBySpecialty(Specialty specialty){
        SpecialtyConfig specialtyConfig = specialtyConfigRepository.findBySpecialty(specialty)
                .orElseThrow(() -> new EntityNotFoundException("Specialty not found"));
        return specialtyConfigMapper.toDto(specialtyConfig);
    }

    @Transactional
    public SpecialtyConfigResponseDto updateSpecialtyConfig(Specialty specialty, SpecialtyConfigUpdateRequestDto specialtyConfigUpdateRequestDto){
        SpecialtyConfig specialtyConfig = specialtyConfigRepository.findBySpecialty(specialty)
                .orElseThrow(() -> new EntityNotFoundException("Specialty config not found"));
        specialtyConfigMapper.updateEntity(specialtyConfigUpdateRequestDto, specialtyConfig);
        SpecialtyConfig updatedSpecialtyConfig = specialtyConfigRepository.save(specialtyConfig);
        return specialtyConfigMapper.toDto(updatedSpecialtyConfig);
    }
}
