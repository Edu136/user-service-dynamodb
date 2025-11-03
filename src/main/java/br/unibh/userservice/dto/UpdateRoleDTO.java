package br.unibh.userservice.dto;

import br.unibh.userservice.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleDTO(
        @NotNull(message = "O valor da role não pode ser nulo.")
        UserRole role
) {
}
