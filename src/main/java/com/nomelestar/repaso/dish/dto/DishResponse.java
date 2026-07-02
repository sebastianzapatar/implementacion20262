package com.nomelestar.repaso.dish.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para devolver los datos de un Plato.
 * En lugar de devolver todo el objeto Chef, devolvemos solo el nombre y ID del chef.
 */
public record DishResponse(
        UUID id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        UUID chefId,
        String nombreChef
) {
}
