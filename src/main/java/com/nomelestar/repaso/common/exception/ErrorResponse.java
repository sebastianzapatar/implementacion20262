package com.nomelestar.repaso.common.exception;

import java.time.LocalDateTime;

/**
 * Estructura estándar de la respuesta de error que se enviará al cliente.
 * Se utiliza un record para simplificar la creación de la clase.
 */
public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp
) {
}
