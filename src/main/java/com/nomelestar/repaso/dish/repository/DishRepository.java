package com.nomelestar.repaso.dish.repository;

import com.nomelestar.repaso.dish.entity.Dish;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad Dish.
 *
 * <p>Este repositorio es el ejemplo de <b>JOINs SIN @Query</b>: todos los métodos
 * de abajo se escriben solo con el NOMBRE del método y Spring Data genera el SQL
 * (incluidos los JOIN) automáticamente. A esto se le llama
 * <i>derived query methods</i> o "consultas derivadas".
 */
public interface DishRepository extends JpaRepository<Dish, UUID> {

    /**
     * Consulta para buscar todos los platos de un chef en particular por su ID.
     * Ya es un JOIN implícito: {@code chef} es una relación @ManyToOne.
     */
    List<Dish> findByChefId(UUID chefId);

    // =========================================================================
    // EJEMPLO 2: JOIN @ManyToMany **SIN** @Query (Derived Query Methods)
    // =========================================================================
    //
    // Cómo se lee el nombre de un método derivado:
    //
    //     findBy | Clientes  | _  | Id
    //     ^^^^^^   ^^^^^^^^    ^    ^^
    //     verbo    atributo   (*)  atributo DENTRO de Client
    //              de Dish
    //
    // (*) El guion bajo "_" marca explícitamente dónde termina una propiedad y
    //     empieza la siguiente. Sin él Spring Data adivina, y si existiera un
    //     campo llamado "clientesId" se equivocaría. Con "_" nunca hay ambigüedad.
    //
    // Como "clientes" es la relación @ManyToMany, Spring Data recorre sola la
    // tabla intermedia dish_clients. NO hay que escribir SQL ni JPQL.
    // =========================================================================

    /**
     * Todos los platos que ha consumido un cliente.
     *
     * <p>SQL generado:
     * <pre>
     *   SELECT d.* FROM dishes d
     *        JOIN dish_clients dc ON dc.dish_id = d.id
     *        JOIN clients cl      ON cl.id = dc.client_id
     *   WHERE cl.id = ?
     * </pre>
     */
    List<Dish> findByClientes_Id(UUID clientId);

    /**
     * <b>Doble JOIN sin @Query</b>: platos que un cliente consumió de un chef
     * concreto, ordenados de más caro a más barato.
     *
     * <p>Hace exactamente lo mismo que
     * {@code ClientRepository.findPlatosDeClientePorChef(...)}, que sí usa
     * {@code @Query}. Comparalos: mismo resultado, cero JPQL escrito a mano.
     *
     * <p>Recorre dos relaciones distintas: {@code clientes} (@ManyToMany, pasa por
     * dish_clients) y {@code chef} (@ManyToOne, usa la FK chef_id).
     */
    List<Dish> findByClientes_IdAndChef_IdOrderByPrecioDesc(UUID clientId, UUID chefId);

    /**
     * JOIN por el <b>email</b> del cliente (no por su id) y con filtro de precio.
     * Muestra que se puede filtrar por cualquier campo de la entidad relacionada,
     * y combinar palabras clave: {@code IgnoreCase}, {@code GreaterThanEqual}.
     */
    List<Dish> findByClientes_EmailIgnoreCaseAndPrecioGreaterThanEqual(String email, BigDecimal precioMinimo);

    /**
     * COUNT con JOIN sin @Query: cuántos platos distintos consumió un cliente.
     * Genera {@code SELECT COUNT(...)}, no trae las filas: mucho más barato que
     * traer la lista y hacer {@code .size()} en Java.
     */
    long countByClientes_Id(UUID clientId);

    /**
     * EXISTS con JOIN sin @Query: ¿este cliente ya consumió este plato?
     * Genera un {@code SELECT ... LIMIT 1}; ideal para validar antes de insertar
     * en la tabla intermedia sin cargar la colección completa en memoria.
     */
    boolean existsByIdAndClientes_Id(UUID dishId, UUID clientId);

    /**
     * {@code @EntityGraph} es la forma de pedir un <b>JOIN FETCH sin escribir
     * {@code @Query}</b>: le dice a JPA que traiga estas relaciones de una vez,
     * en la misma consulta, en lugar de dejarlas LAZY.
     *
     * <p>Sirve para matar el problema N+1 conservando el método derivado.
     */
    @EntityGraph(attributePaths = {"chef"})
    List<Dish> findByClientes_Nombre(String nombreCliente);

    /**
     * Mismo truco aplicado al listado general: trae cada plato junto con su chef
     * en una sola consulta, en vez de una consulta extra por plato.
     *
     * <p>Se llama {@code findAllBy} (con "By" y sin condiciones) porque
     * {@code findAll()} viene de JpaRepository y no admite {@code @EntityGraph}
     * declarado aquí.
     */
    @EntityGraph(attributePaths = {"chef"})
    List<Dish> findAllBy();
}
