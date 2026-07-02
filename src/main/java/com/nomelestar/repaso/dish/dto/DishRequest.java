package com.nomelestar.repaso.dish.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para crear o actualizar un Plato.
 * Incluye el chefId para poder asociar el plato a un Chef existente.
 */
public record DishRequest(
        String nombre,
        String descripcion,
        BigDecimal precio,
        UUID chefId
) {
}
