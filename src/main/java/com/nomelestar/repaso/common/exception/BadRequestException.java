package com.nomelestar.repaso.common.exception;

/**
 * Excepción personalizada para representar un error de validación o 
 * solicitud incorrecta por parte del usuario (HTTP 400).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
