package com.marquesdev.clinica.mapper;

import com.marquesdev.clinica.dto.ClientRequestDto;
import com.marquesdev.clinica.dto.ClientResponseDto;
import com.marquesdev.clinica.dto.ClientUpdateRequestDto;
import com.marquesdev.clinica.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)

public interface ClientMapper {

    @Mapping(target = "street", ignore = true)
    @Mapping(target = "neighborhood", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "state", ignore = true)
    Client toClient(ClientRequestDto dto);

    void updateClient(ClientUpdateRequestDto dto, @MappingTarget Client client);

    @Mapping(target = "email", source = "user.email")
    ClientResponseDto toDto(Client client);

}
