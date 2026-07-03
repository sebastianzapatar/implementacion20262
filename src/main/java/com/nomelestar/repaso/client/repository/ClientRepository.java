package com.nomelestar.repaso.client.repository;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.dish.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Client.
 * Incluye consultas JPQL con JOINs para estadísticas del restaurante.
 */
public interface ClientRepository extends JpaRepository<Client, UUID> {

    /**
     * Query Method derivado: buscar cliente por email.
     */
    Optional<Client> findByEmail(String email);

    /**
     * JOIN JPQL: Obtener el Chef con más platos vendidos (comprados por clientes).
     * Recorre la tabla intermedia dish_clients y cuenta cuántas veces los platos
     * de cada chef han sido consumidos. Retorna el chef con mayor conteo.
     */
    @Query("""
        SELECT d.chef FROM Client c
        JOIN c.platosConsumidos d
        GROUP BY d.chef
        ORDER BY COUNT(d) DESC
        LIMIT 1
    """)
    Optional<Chef> findChefConMasVentas();

    /**
     * JOIN JPQL: Obtener los platos que un cliente ha consumido de un chef específico.
     * Útil para saber las preferencias de un cliente respecto a un chef.
     */
    @Query("""
        SELECT d FROM Client c
        JOIN c.platosConsumidos d
        WHERE c.id = :clientId AND d.chef.id = :chefId
    """)
    List<Dish> findPlatosDeClientePorChef(@Param("clientId") UUID clientId, @Param("chefId") UUID chefId);
}
