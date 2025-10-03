package br.unibh.userservice.dto;

import br.unibh.userservice.entity.UserRole;

public record UpdateRoleDTO(
        UserRole role
) {
}
