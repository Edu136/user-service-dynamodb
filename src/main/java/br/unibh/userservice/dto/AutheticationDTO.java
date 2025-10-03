package br.unibh.userservice.dto;

import jakarta.validation.constraints.NotNull;

public record AutheticationDTO(
        @NotNull(message = "Login não pode ser nulo.")
        String login,
        @NotNull(message = "Password não pode ser nulo.")
        String password
) {
}
