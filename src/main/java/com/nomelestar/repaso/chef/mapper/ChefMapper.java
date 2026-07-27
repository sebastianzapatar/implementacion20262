package com.nomelestar.repaso.chef.mapper;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.dish.entity.Dish;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase utilitaria para mapear (convertir) entre Entidades y DTOs.
 * Usamos métodos estáticos para no tener que instanciar esta clase.
 * Este enfoque es "manual", excelente para comprender cómo viajan los datos entre capas,
 * aunque en proyectos grandes se suelen usar librerías como MapStruct.
 */
public class ChefMapper {

    private ChefMapper() {
        // Constructor privado para evitar que la clase sea instanciada.
    }

    /**
     * Convierte un DTO de entrada (ChefRequest) en una Entidad (Chef).
     * Ideal para guardar datos en la base de datos.
     */
    public static Chef toEntity(ChefRequest request) {
        if (request == null) {
            return null;
        }

        Chef chef = Chef.builder()
                .nombre(request.nombre())
                .build();

        if (request.platos() != null) {
            for (var platoReq : request.platos()) {
                Dish dish = Dish.builder()
                        .nombre(platoReq.nombre())
                        .descripcion(platoReq.descripcion())
                        .precio(platoReq.precio())
                        .chef(chef)
                        .build();
                chef.getPlatos().add(dish);
            }
        }

        return chef;
    }

    /**
     * Convierte una Entidad (Chef) en un DTO de salida (ChefResponse).
     * Ideal para retornar datos al cliente evitando exponer la entidad de BD.
     */
    public static ChefResponse toResponse(Chef entity) {
        if (entity == null) {
            return null;
        }

        // Extraemos solo los nombres de los platos de la entidad
        List<String> nombresPlatos = entity.getPlatos() == null 
                ? List.of() 
                : entity.getPlatos().stream()
                        .map(Dish::getNombre)
                        .collect(Collectors.toList());

        return new ChefResponse(
                entity.getId(),
                entity.getNombre(),
                nombresPlatos
        );
    }
}
