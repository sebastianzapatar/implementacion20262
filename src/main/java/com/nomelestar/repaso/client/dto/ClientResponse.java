package com.nomelestar.repaso.client.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO para devolver los datos de un Cliente.
 * Incluye la lista de nombres de los platos que ha consumido.
 */
public record ClientResponse(
        UUID id,
        String nombre,
        String email,
        List<String> platosConsumidos
) {
}
