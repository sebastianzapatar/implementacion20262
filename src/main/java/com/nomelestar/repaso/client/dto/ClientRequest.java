package com.nomelestar.repaso.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un Cliente.
 */
public record ClientRequest(
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El email debe ser válido")
        String email
) {
}
