package com.nomelestar.repaso.chef.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Record DTO (Data Transfer Object) para recibir datos del cliente (por ejemplo en POST y PUT).
 * Los records en Java a partir de la versión 14 son estructuras inmutables ideales para DTOs,
 * ya que por defecto traen constructores, getters, equals, hashCode y toString.
 * 
 * @param nombre el nombre del chef que nos envía el cliente
 */
public record ChefRequest(
        @NotBlank(message = "El nombre del chef no puede estar vacío")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        List<DishForChefRequest> platos
) {
}
