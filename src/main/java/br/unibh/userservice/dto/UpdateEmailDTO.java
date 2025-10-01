package br.unibh.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UpdateEmailDTO (
        @NotNull(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email
){
}
