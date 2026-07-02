package com.nomelestar.repaso.chef.dto;

import java.util.List;
import java.util.UUID;

/**
 * Record DTO para enviar los datos del Chef al cliente.
 * Ayuda a encapsular la entidad de la base de datos y evitar exponer información sensible,
 * permitiendo devolver solo los campos estrictamente necesarios para la vista.
 *
 * @param id Identificador único del chef
 * @param nombre Nombre del chef
 * @param platos Lista de nombres de los platos creados por el chef
 */
public record ChefResponse(
        UUID id,
        String nombre,
        List<String> platos
) {
}
