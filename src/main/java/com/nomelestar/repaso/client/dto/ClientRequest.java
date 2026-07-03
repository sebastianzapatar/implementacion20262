package com.nomelestar.repaso.client.dto;

/**
 * DTO para crear o actualizar un Cliente.
 */
public record ClientRequest(
        String nombre,
        String email
) {
}
