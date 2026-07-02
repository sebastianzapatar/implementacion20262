package com.nomelestar.repaso.dish.mapper;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.entity.Dish;

public class DishMapper {

    private DishMapper() {}

    /**
     * Convierte el DTO a Entidad. Recibe el Chef para asociarlo directamente.
     */
    public static Dish toEntity(DishRequest request, Chef chef) {
        if (request == null) return null;

        return Dish.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio(request.precio())
                .chef(chef)
                .build();
    }

    /**
     * Convierte la Entidad a DTO de respuesta.
     */
    public static DishResponse toResponse(Dish entity) {
        if (entity == null) return null;

        return new DishResponse(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getPrecio(),
                entity.getChef() != null ? entity.getChef().getId() : null,
                entity.getChef() != null ? entity.getChef().getNombre() : null
        );
    }
}
