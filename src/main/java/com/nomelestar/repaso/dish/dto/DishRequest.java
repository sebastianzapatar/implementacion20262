package com.nomelestar.repaso.dish.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear o actualizar un Plato.
 * Incluye el chefId para poder asociar el plato a un Chef existente.
 */
public record DishRequest(
        @NotBlank(message = "El nombre del plato no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
        BigDecimal precio,

        @NotNull(message = "El ID del chef es obligatorio")
        UUID chefId
) {
}
