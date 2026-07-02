package com.nomelestar.repaso.common.exception;

/**
 * Excepción personalizada para representar un recurso no encontrado (HTTP 404).
 * Esta excepción es de tipo RuntimeException (no chequeada).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
