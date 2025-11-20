package br.unibh.userservice.dto;

import br.unibh.userservice.entity.UserRole;

public record LoginResponseDTO(
        String token,
        String idUser,
        UserRole role,
        String username,
        String email
) {
}
