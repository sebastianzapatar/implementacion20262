package com.nomelestar.repaso.e2e;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.client.dto.ClientRequest;
import com.nomelestar.repaso.client.dto.ClientResponse;
import com.nomelestar.repaso.client.repository.ClientRepository;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.repository.DishRepository;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas End-to-End (E2E) para el módulo de Clientes.
 * Levanta la aplicación completa en un puerto aleatorio con H2
 * y prueba los endpoints HTTP de forma real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Client — Pruebas End-to-End")
class ClientE2ETest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private ChefRepository chefRepository;

    private String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        dishRepository.deleteAll();
        chefRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Flujo completo: crear chef → plato → cliente → consumir → stats")
    void flujoCompleto() {
        // 1. Crear un Chef
        ChefResponse chef = restTemplate.postForObject(
                baseUrl() + "/chefs", new ChefRequest("Chef E2E"), ChefResponse.class);
        assertNotNull(chef);
        assertNotNull(chef.id());

        // 2. Crear un Plato asociado al Chef
        DishResponse dish = restTemplate.postForObject(
                baseUrl() + "/dishes",
                new DishRequest("Pizza E2E", "Pizza de prueba", new BigDecimal("15.00"), chef.id()),
                DishResponse.class);
        assertNotNull(dish);
        assertNotNull(dish.id());

        // 3. Crear un Cliente
        ClientResponse client = restTemplate.postForObject(
                baseUrl() + "/clients", new ClientRequest("Cliente E2E", "e2e@test.com"), ClientResponse.class);
        assertNotNull(client);
        assertNotNull(client.id());
        assertTrue(client.platosConsumidos().isEmpty());

        // 4. Registrar consumo del plato por el cliente
        ClientResponse clientConPlato = restTemplate.postForObject(
                baseUrl() + "/clients/" + client.id() + "/dishes/" + dish.id(),
                null, ClientResponse.class);
        assertNotNull(clientConPlato);
        assertEquals(1, clientConPlato.platosConsumidos().size());
        assertEquals("Pizza E2E", clientConPlato.platosConsumidos().get(0));

        // 5. Consultar chef con más ventas
        ChefResponse chefMasVendido = restTemplate.getForObject(
                baseUrl() + "/clients/stats/chef-mas-vendido", ChefResponse.class);
        assertNotNull(chefMasVendido);
        assertEquals("Chef E2E", chefMasVendido.nombre());

        // 6. Consultar platos preferidos del cliente por chef
        DishResponse[] platosPreferidos = restTemplate.getForObject(
                baseUrl() + "/clients/" + client.id() + "/chefs/" + chef.id() + "/platos",
                DishResponse[].class);
        assertNotNull(platosPreferidos);
        assertEquals(1, platosPreferidos.length);
        assertEquals("Pizza E2E", platosPreferidos[0].nombre());
    }

    @Test
    @Order(2)
    @DisplayName("CRUD completo de un cliente")
    void crudCliente() {
        // Crear
        ClientResponse created = restTemplate.postForObject(
                baseUrl() + "/clients", new ClientRequest("Test CRUD", "crud@test.com"), ClientResponse.class);
        assertNotNull(created);

        // Leer
        ClientResponse read = restTemplate.getForObject(
                baseUrl() + "/clients/" + created.id(), ClientResponse.class);
        assertNotNull(read);
        assertEquals("Test CRUD", read.nombre());

        // Actualizar
        restTemplate.put(baseUrl() + "/clients/" + created.id(),
                new ClientRequest("Updated CRUD", "updated@test.com"));
        ClientResponse updated = restTemplate.getForObject(
                baseUrl() + "/clients/" + created.id(), ClientResponse.class);
        assertEquals("Updated CRUD", updated.nombre());

        // Eliminar
        restTemplate.delete(baseUrl() + "/clients/" + created.id());
        org.springframework.web.client.HttpClientErrorException exception = assertThrows(
                org.springframework.web.client.HttpClientErrorException.class,
                () -> restTemplate.getForEntity(baseUrl() + "/clients/" + created.id(), String.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
