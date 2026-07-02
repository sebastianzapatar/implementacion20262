package com.nomelestar.repaso.dish.repository;

import com.nomelestar.repaso.dish.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad Dish.
 */
public interface DishRepository extends JpaRepository<Dish, UUID> {

    /**
     * Consulta para buscar todos los platos de un chef en particular por su ID.
     */
    List<Dish> findByChefId(UUID chefId);
}
