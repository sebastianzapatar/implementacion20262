package com.nomelestar.repaso.client.repository;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.dish.dto.DishPopularityResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    // =========================================================================
    // EJEMPLO 1: CONSULTA COMPLEJA @ManyToMany **CON** @Query (JPQL)
    // =========================================================================

    /**
     * Ranking de los platos más consumidos, atravesando la relación
     * Muchos-a-Muchos {@code Client <-> Dish} y además la Muchos-a-Uno
     * {@code Dish -> Chef}.
     *
     * <p>Piezas de JPQL que se combinan aquí (por eso es la consulta "compleja"):
     * <ol>
     *   <li><b>Doble JOIN</b>: {@code d.clientes} recorre la tabla intermedia
     *       {@code dish_clients}, y {@code d.chef} recorre la llave foránea
     *       {@code chef_id}. Nunca nombramos la tabla intermedia: en JPQL se
     *       navega por los ATRIBUTOS de las entidades, no por tablas.</li>
     *   <li><b>WHERE</b>: filtra por precio mínimo antes de agrupar.</li>
     *   <li><b>GROUP BY</b>: agrupa por plato. Debe incluir TODOS los campos no
     *       agregados del SELECT, o Postgres lanza error.</li>
     *   <li><b>HAVING</b>: filtra sobre el resultado del agregado (el WHERE no
     *       puede usar COUNT; el HAVING sí).</li>
     *   <li><b>COUNT(DISTINCT ...)</b>: cuenta clientes únicos.</li>
     *   <li><b>SUM(d.precio)</b>: como el JOIN genera una fila por cada pareja
     *       (plato, cliente), sumar el precio equivale a precio * nº clientes.</li>
     *   <li><b>Constructor expression</b>: devuelve un DTO plano en vez de
     *       entidades, así no se cargan colecciones LAZY.</li>
     * </ol>
     *
     * <p>SQL aproximado que genera Hibernate:
     * <pre>
     *   SELECT d.id, d.nombre, ch.nombre, d.precio,
     *          COUNT(DISTINCT cl.id), SUM(d.precio)
     *   FROM dishes d
     *        JOIN dish_clients dc ON dc.dish_id = d.id
     *        JOIN clients cl      ON cl.id = dc.client_id
     *        JOIN chefs ch        ON ch.id = d.chef_id
     *   WHERE d.precio &gt;= ?
     *   GROUP BY d.id, d.nombre, ch.nombre, d.precio
     *   HAVING COUNT(DISTINCT cl.id) &gt;= ?
     *   ORDER BY COUNT(DISTINCT cl.id) DESC, d.precio DESC
     * </pre>
     *
     * @param precioMinimo    solo considera platos que cuesten al menos esto
     * @param minimoClientes  solo devuelve platos con al menos esta cantidad de clientes
     */
    @Query("""
            SELECT new com.nomelestar.repaso.dish.dto.DishPopularityResponse(
                       d.id,
                       d.nombre,
                       ch.nombre,
                       d.precio,
                       COUNT(DISTINCT cl.id),
                       SUM(d.precio)
                   )
            FROM Dish d
                 JOIN d.clientes cl
                 JOIN d.chef ch
            WHERE d.precio >= :precioMinimo
            GROUP BY d.id, d.nombre, ch.nombre, d.precio
            HAVING COUNT(DISTINCT cl.id) >= :minimoClientes
            ORDER BY COUNT(DISTINCT cl.id) DESC, d.precio DESC
            """)
    List<DishPopularityResponse> rankingPlatosMasConsumidos(
            @Param("precioMinimo") BigDecimal precioMinimo,
            @Param("minimoClientes") long minimoClientes);

    /**
     * Variante con <b>JOIN FETCH</b>: trae los clientes junto con sus platos y el
     * chef de cada plato en UNA sola consulta.
     *
     * <p>Sirve para evitar el problema <b>N+1</b>: sin esto, listar 100 clientes
     * lanza 1 consulta para los clientes + 100 para sus platos + 1 por cada chef.
     *
     * <p>{@code DISTINCT} es necesario porque el JOIN FETCH sobre una colección
     * duplica la fila del cliente una vez por cada plato que consumió.
     *
     * <p>{@code LEFT} para no perder a los clientes que aún no han consumido nada
     * (un JOIN normal los excluiría).
     */
    @Query("""
            SELECT DISTINCT c FROM Client c
                 LEFT JOIN FETCH c.platosConsumidos d
                 LEFT JOIN FETCH d.chef
            """)
    List<Client> findAllConPlatosYChef();
}
