package com.nomelestar.repaso.chef.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO para crear platos simultáneamente junto con un Chef.
 * No requiere chefId porque el chefId es el del Chef que se está creando.
 */
public record DishForChefRequest(
        @NotBlank(message = "El nombre del plato no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor que cero")
        BigDecimal precio
) {
}
