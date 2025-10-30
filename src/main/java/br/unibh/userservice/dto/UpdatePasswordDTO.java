package br.unibh.userservice.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePasswordDTO(
        @NotNull(message = "Password é obrigatório.")
        String newPassword,
        @NotNull(message = "Old password é obrigatório.")
        String oldPassword
){
}
