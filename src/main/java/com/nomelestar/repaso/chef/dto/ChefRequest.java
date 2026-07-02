package com.nomelestar.repaso.chef.dto;

/**
 * Record DTO (Data Transfer Object) para recibir datos del cliente (por ejemplo en POST y PUT).
 * Los records en Java a partir de la versión 14 son estructuras inmutables ideales para DTOs,
 * ya que por defecto traen constructores, getters, equals, hashCode y toString.
 * 
 * @param nombre el nombre del chef que nos envía el cliente
 */
public record ChefRequest(
        String nombre
) {
}
