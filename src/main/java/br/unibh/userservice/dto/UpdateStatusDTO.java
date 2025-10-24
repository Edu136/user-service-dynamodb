package br.unibh.userservice.dto;

import br.unibh.userservice.entity.UserState;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusDTO(
        @NotNull(message = "O status do usuário não pode ser nulo")
        UserState userState
) {
}
