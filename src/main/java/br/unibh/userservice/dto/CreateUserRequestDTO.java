package br.unibh.userservice.dto;

import jakarta.validation.constraints.NotNull;

public record CreateUserRequestDTO (
        @NotNull(message = "Username é obrigatório.")
        String username,
        @NotNull(message = "Email é obrigatório.")
        String email,
        @NotNull(message = "Password é obrigatório.")
        String password){
}
