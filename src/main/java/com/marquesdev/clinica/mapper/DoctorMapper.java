package com.marquesdev.clinica.mapper;

import com.marquesdev.clinica.dto.DoctorRequestDto;
import com.marquesdev.clinica.dto.DoctorResponseDto;
import com.marquesdev.clinica.dto.DoctorUpdateRequestDto;
import com.marquesdev.clinica.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DoctorMapper {

    Doctor toDoctor(DoctorRequestDto dto);

    void updateDoctor(DoctorUpdateRequestDto dto, @MappingTarget Doctor doctor);

    @Mapping(target = "email", source = "user.email")
    DoctorResponseDto toDto(Doctor doctor);
}
