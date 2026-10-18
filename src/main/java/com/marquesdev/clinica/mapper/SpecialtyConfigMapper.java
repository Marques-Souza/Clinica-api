package com.marquesdev.clinica.mapper;

import com.marquesdev.clinica.dto.SpecialtyConfigRequestDto;
import com.marquesdev.clinica.dto.SpecialtyConfigResponseDto;
import com.marquesdev.clinica.dto.SpecialtyConfigUpdateRequestDto;
import com.marquesdev.clinica.entity.SpecialtyConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SpecialtyConfigMapper {

    @Mapping(target = "id", ignore = true)
    SpecialtyConfig toEntity(SpecialtyConfigRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    void updateEntity(
            SpecialtyConfigUpdateRequestDto dto,
            @MappingTarget SpecialtyConfig specialtyConfig
    );

    SpecialtyConfigResponseDto toDto(SpecialtyConfig specialtyConfig);

}
