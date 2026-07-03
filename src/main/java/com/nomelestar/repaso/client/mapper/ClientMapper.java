package com.nomelestar.repaso.client.mapper;

import com.nomelestar.repaso.client.dto.ClientRequest;
import com.nomelestar.repaso.client.dto.ClientResponse;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.dish.entity.Dish;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase utilitaria para mapear entre la entidad Client y sus DTOs.
 */
public class ClientMapper {

    private ClientMapper() {}

    /**
     * Convierte un DTO de entrada (ClientRequest) en una Entidad (Client).
     */
    public static Client toEntity(ClientRequest request) {
        if (request == null) return null;

        return Client.builder()
                .nombre(request.nombre())
                .email(request.email())
                .build();
    }

    /**
     * Convierte una Entidad (Client) en un DTO de salida (ClientResponse).
     */
    public static ClientResponse toResponse(Client entity) {
        if (entity == null) return null;

        List<String> nombresPlatos = entity.getPlatosConsumidos() == null
                ? List.of()
                : entity.getPlatosConsumidos().stream()
                        .map(Dish::getNombre)
                        .collect(Collectors.toList());

        return new ClientResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getEmail(),
                nombresPlatos
        );
    }
}
