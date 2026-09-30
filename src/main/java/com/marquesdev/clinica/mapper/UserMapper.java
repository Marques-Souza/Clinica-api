package com.marquesdev.clinica.mapper;


import com.marquesdev.clinica.dto.UserRequestDto;
import com.marquesdev.clinica.dto.UserResponseDto;
import com.marquesdev.clinica.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserRequestDto dto);

    @Mapping(target = "role", source = "user.perfil")
    @Mapping(target = "url", source = "url")
    UserResponseDto toDto(User user, String url);
}
