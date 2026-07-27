package com.nomelestar.repaso.query;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.client.repository.ClientRepository;
import com.nomelestar.repaso.dish.dto.DishPopularityResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.repository.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de las consultas sobre la relación Muchos a Muchos {@code Client <-> Dish}.
 *
 * <p>@DataJpaTest levanta SOLO la capa de persistencia (entidades + repositorios)
 * contra una base H2 en memoria, sin arrancar el servidor web. Es rápido y cada
 * test corre dentro de una transacción que se revierte al terminar, así que las
 * pruebas no se pisan entre sí.
 *
 * <p>El valor de estos tests: si una consulta JPQL tiene un error de sintaxis o
 * un nombre de campo mal escrito, se detecta acá y no en producción.
 *
 * <p>Escenario que se arma en cada test:
 * <pre>
 *   Chef Gustavo -> Pasta ($20.000), Risotto ($35.000)
 *   Chef Marta   -> Ceviche ($30.000)
 *
 *   Ana   consumió Pasta y Risotto
 *   Bruno consumió Pasta y Ceviche
 *   Carla consumió Pasta
 * </pre>
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Consultas ManyToMany: con @Query y sin @Query")
class ManyToManyQueryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private ChefRepository chefRepository;

    private Chef gustavo;
    private Chef marta;
    private Dish pasta;
    private Dish risotto;
    private Dish ceviche;
    private Client ana;
    private Client bruno;
    private Client carla;

    @BeforeEach
    void prepararDatos() {
        gustavo = entityManager.persist(Chef.builder().nombre("Gustavo").build());
        marta = entityManager.persist(Chef.builder().nombre("Marta").build());

        pasta = entityManager.persist(nuevoPlato("Pasta", "20000", gustavo));
        risotto = entityManager.persist(nuevoPlato("Risotto", "35000", gustavo));
        ceviche = entityManager.persist(nuevoPlato("Ceviche", "30000", marta));

        ana = entityManager.persist(nuevoCliente("Ana", "ana@test.com", List.of(pasta, risotto)));
        bruno = entityManager.persist(nuevoCliente("Bruno", "bruno@test.com", List.of(pasta, ceviche)));
        carla = entityManager.persist(nuevoCliente("Carla", "carla@test.com", List.of(pasta)));

        // Baja los INSERT a la BD y limpia el caché de primer nivel, para que las
        // consultas de abajo lean de verdad de la base y no de memoria.
        entityManager.flush();
        entityManager.clear();
    }

    // =========================================================================
    // EJEMPLO 1: consulta compleja CON @Query
    // =========================================================================

    @Test
    @DisplayName("@Query: el ranking agrupa, cuenta clientes distintos y suma ingresos")
    void rankingPlatosMasConsumidos() {
        List<DishPopularityResponse> ranking =
                clientRepository.rankingPlatosMasConsumidos(BigDecimal.ZERO, 1L);

        // Los 3 platos tienen al menos 1 cliente
        assertThat(ranking).hasSize(3);

        // Ordenado por cantidad de clientes DESC -> Pasta primero (3 clientes)
        DishPopularityResponse primero = ranking.getFirst();
        assertThat(primero.nombrePlato()).isEqualTo("Pasta");
        assertThat(primero.nombreChef()).isEqualTo("Gustavo");
        assertThat(primero.totalClientes()).isEqualTo(3L);
        // SUM(precio) sobre 3 filas del JOIN = 20.000 * 3
        assertThat(primero.ingresoEstimado()).isEqualByComparingTo("60000");
    }

    @Test
    @DisplayName("@Query: HAVING descarta los platos con pocos clientes")
    void rankingFiltraPorMinimoDeClientes() {
        // Solo Pasta llega a 2 clientes o más (tiene 3)
        List<DishPopularityResponse> ranking =
                clientRepository.rankingPlatosMasConsumidos(BigDecimal.ZERO, 2L);

        assertThat(ranking)
                .extracting(DishPopularityResponse::nombrePlato)
                .containsExactly("Pasta");
    }

    @Test
    @DisplayName("@Query: WHERE descarta los platos por debajo del precio mínimo")
    void rankingFiltraPorPrecioMinimo() {
        // Deja fuera la Pasta (20.000); quedan Risotto (35.000) y Ceviche (30.000)
        List<DishPopularityResponse> ranking =
                clientRepository.rankingPlatosMasConsumidos(new BigDecimal("25000"), 1L);

        assertThat(ranking)
                .extracting(DishPopularityResponse::nombrePlato)
                .containsExactlyInAnyOrder("Risotto", "Ceviche");
    }

    @Test
    @DisplayName("@Query con JOIN FETCH: trae clientes, platos y chef sin fallar por LAZY")
    void findAllConPlatosYChef() {
        List<Client> clientes = clientRepository.findAllConPlatosYChef();

        assertThat(clientes).hasSize(3);

        Client anaCargada = clientes.stream()
                .filter(c -> c.getEmail().equals("ana@test.com"))
                .findFirst()
                .orElseThrow();

        // Si el JOIN FETCH no funcionara, esto lanzaría LazyInitializationException
        assertThat(anaCargada.getPlatosConsumidos())
                .extracting(Dish::getNombre)
                .containsExactlyInAnyOrder("Pasta", "Risotto");

        // El chef de cada plato también viene cargado en la misma consulta
        assertThat(anaCargada.getPlatosConsumidos())
                .extracting(d -> d.getChef().getNombre())
                .containsOnly("Gustavo");
    }

    // =========================================================================
    // EJEMPLO 2: JOINs SIN @Query (métodos derivados)
    // =========================================================================

    @Test
    @DisplayName("Sin @Query: findByClientes_Id hace el JOIN con dish_clients")
    void findByClientesId() {
        List<Dish> platosDeAna = dishRepository.findByClientes_Id(ana.getId());

        assertThat(platosDeAna)
                .extracting(Dish::getNombre)
                .containsExactlyInAnyOrder("Pasta", "Risotto");
    }

    @Test
    @DisplayName("Sin @Query: doble JOIN (cliente + chef) con orden por precio")
    void findByClientesIdAndChefId() {
        List<Dish> platos =
                dishRepository.findByClientes_IdAndChef_IdOrderByPrecioDesc(ana.getId(), gustavo.getId());

        // Ana consumió los dos platos de Gustavo, del más caro al más barato
        assertThat(platos)
                .extracting(Dish::getNombre)
                .containsExactly("Risotto", "Pasta");
    }

    @Test
    @DisplayName("Sin @Query vs con @Query: los dos enfoques dan el mismo resultado")
    void ambosEnfoquesCoinciden() {
        List<String> sinQuery =
                dishRepository.findByClientes_IdAndChef_IdOrderByPrecioDesc(bruno.getId(), marta.getId())
                        .stream().map(Dish::getNombre).toList();

        List<String> conQuery =
                clientRepository.findPlatosDeClientePorChef(bruno.getId(), marta.getId())
                        .stream().map(Dish::getNombre).toList();

        assertThat(sinQuery).containsExactlyInAnyOrderElementsOf(conQuery);
        assertThat(sinQuery).containsExactly("Ceviche");
    }

    @Test
    @DisplayName("Sin @Query: JOIN filtrando por email del cliente y precio mínimo")
    void findByClientesEmailYPrecio() {
        List<Dish> platos = dishRepository
                .findByClientes_EmailIgnoreCaseAndPrecioGreaterThanEqual("ANA@TEST.COM", new BigDecimal("25000"));

        // De los platos de Ana (Pasta 20.000, Risotto 35.000) solo pasa el Risotto
        assertThat(platos)
                .extracting(Dish::getNombre)
                .containsExactly("Risotto");
    }

    @Test
    @DisplayName("Sin @Query: COUNT y EXISTS sobre la tabla intermedia")
    void countYExists() {
        assertThat(dishRepository.countByClientes_Id(ana.getId())).isEqualTo(2);
        assertThat(dishRepository.countByClientes_Id(carla.getId())).isEqualTo(1);

        assertThat(dishRepository.existsByIdAndClientes_Id(pasta.getId(), carla.getId())).isTrue();
        assertThat(dishRepository.existsByIdAndClientes_Id(risotto.getId(), carla.getId())).isFalse();
    }

    @Test
    @DisplayName("Sin @Query: @EntityGraph trae el chef junto con el plato")
    void entityGraphTraeElChef() {
        List<Dish> platos = dishRepository.findByClientes_Nombre("Bruno");

        assertThat(platos)
                .extracting(d -> d.getChef().getNombre())
                .containsExactlyInAnyOrder("Gustavo", "Marta");
    }

    // =========================================================================
    // Consultas que ya existían: verificamos que sigan funcionando
    // =========================================================================

    @Test
    @DisplayName("@Query: el chef con más ventas es el que más platos consumidos acumula")
    void findChefConMasVentas() {
        // Gustavo: Pasta (3) + Risotto (1) = 4 consumos. Marta: Ceviche (1) = 1.
        assertThat(clientRepository.findChefConMasVentas())
                .isPresent()
                .get()
                .extracting(Chef::getNombre)
                .isEqualTo("Gustavo");
    }

    @Test
    @DisplayName("@EntityGraph en ChefRepository: trae los chefs con sus platos")
    void chefFindAllByTraePlatos() {
        List<Chef> chefs = chefRepository.findAllBy();

        assertThat(chefs).hasSize(2);
        Chef gustavoCargado = chefs.stream()
                .filter(c -> c.getNombre().equals("Gustavo"))
                .findFirst()
                .orElseThrow();

        assertThat(gustavoCargado.getPlatos())
                .extracting(Dish::getNombre)
                .containsExactlyInAnyOrder("Pasta", "Risotto");
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private Dish nuevoPlato(String nombre, String precio, Chef chef) {
        return Dish.builder()
                .nombre(nombre)
                .descripcion("Plato de prueba: " + nombre)
                .precio(new BigDecimal(precio))
                .chef(chef)
                .build();
    }

    private Client nuevoCliente(String nombre, String email, List<Dish> platos) {
        Client client = Client.builder()
                .nombre(nombre)
                .email(email)
                .build();
        client.getPlatosConsumidos().addAll(platos);
        return client;
    }
}
