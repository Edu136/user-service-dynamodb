package br.unibh.userservice.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordDTO(
        @NotNull(message = "Password é obrigatório.")
        String password
){
}
