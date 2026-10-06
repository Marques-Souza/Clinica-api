package com.marquesdev.clinica.mapper;

import com.marquesdev.clinica.dto.ClientRequestDto;
import com.marquesdev.clinica.dto.ClientResponseDto;
import com.marquesdev.clinica.entity.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    Client toClient(ClientRequestDto dto);

    ClientResponseDto toDto(Client client);

}
