package br.unibh.userservice.dto;

import br.unibh.userservice.entity.UserState;

import java.time.LocalDateTime;

public record UserResponseDTO (
        String id,
        String username,
        String email,
        UserState status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){}
