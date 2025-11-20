package br.unibh.userservice.mapper;

import br.unibh.userservice.dto.CreateUserRequestDTO;
import br.unibh.userservice.dto.UserResponseDTO;
import br.unibh.userservice.dto.UserUpdateResponseDTO;
import br.unibh.userservice.entity.User;
import br.unibh.userservice.entity.UserRole;
import br.unibh.userservice.entity.UserState;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring",
        imports = {UUID.class, LocalDateTime.class, UserState.class, UserRole.class})
public interface UserMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "username", expression = "java(request.username().trim().toLowerCase())")
    @Mapping(target = "email", expression = "java(request.email().trim().toLowerCase())")
    @Mapping(target = "status", expression = "java(UserState.ACTIVE)")
    @Mapping(target = "role", expression = "java(UserRole.USER)")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    User toEntity(CreateUserRequestDTO request, String encodedPassword);

    UserResponseDTO toResponseDto(User user);

    @Mapping(target = "token", source = "token")
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "updatedAt", source = "user.updatedAt")
    @Mapping(target = "role", source = "user.role")
    UserUpdateResponseDTO toUpdateResponseDto(User user, String token);
}