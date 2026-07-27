package com.nomelestar.repaso.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura estándar de la respuesta de error que se enviará al cliente.
 * Se utiliza un record para simplificar la creación de la clase.
 *
 * @param message          descripción legible del error
 * @param status           código HTTP (400, 404, 409, 500...)
 * @param timestamp        momento en que ocurrió
 * @param validationErrors detalle campo -> mensaje, solo en errores de validación.
 *                         Con @JsonInclude(NON_NULL) no aparece en el JSON cuando
 *                         es null, así el resto de las respuestas no cambia de forma.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp,
        Map<String, String> validationErrors
) {
    /**
     * Constructor corto para los errores que no son de validación.
     */
    public ErrorResponse(String message, int status, LocalDateTime timestamp) {
        this(message, status, timestamp, null);
    }
}
