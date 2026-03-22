package com.fluxbank.auth.application.mapper;

import com.fluxbank.auth.application.dto.UserDto;
import com.fluxbank.auth.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserDto toDto(User user);
}
