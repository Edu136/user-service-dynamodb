package br.unibh.userservice.dto;

public record ValidationResultDTO(
        boolean valid,
        String message
) {
}
