package br.unibh.userservice.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUsernameDTO(
        @NotNull(message = "Username é obrigatório.")
        String username
) {
}
