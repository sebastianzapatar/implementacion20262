package com.nomelestar.repaso.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.client.repository.ClientRepository;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.repository.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para Client usando H2 en memoria.
 * Valida las queries JPQL con JOIN para la relación muchos a muchos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Client — Pruebas de Integración (Repository + H2)")
class ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ChefRepository chefRepository;

    @Autowired
    private DishRepository dishRepository;

    private Chef chef1;
    private Chef chef2;
    private Dish pizza;
    private Dish sushi;
    private Dish ensalada;

    @BeforeEach
    void setUp() {
        // Limpiar datos en orden correcto para respetar constraints
        clientRepository.deleteAll();
        dishRepository.deleteAll();
        chefRepository.deleteAll();

        // Crear chefs
        chef1 = chefRepository.save(Chef.builder().nombre("Chef Mario").build());
        chef2 = chefRepository.save(Chef.builder().nombre("Chef Yoshi").build());

        // Crear platos
        pizza = dishRepository.save(Dish.builder()
                .nombre("Pizza").descripcion("Pizza italiana").precio(new BigDecimal("12.50")).chef(chef1).build());
        sushi = dishRepository.save(Dish.builder()
                .nombre("Sushi").descripcion("Sushi japonés").precio(new BigDecimal("18.00")).chef(chef2).build());
        ensalada = dishRepository.save(Dish.builder()
                .nombre("Ensalada").descripcion("Ensalada mixta").precio(new BigDecimal("8.00")).chef(chef1).build());
    }

    @Test
    @DisplayName("CRUD: crear y obtener un cliente por API")
    void crearYObtenerCliente() throws Exception {
        String json = """
                {"nombre": "Juan", "email": "juan@email.com"}
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    @DisplayName("ManyToMany: registrar consumo de plato y verificar relación")
    void registrarConsumoYVerificar() throws Exception {
        // Crear cliente
        String clientJson = """
                {"nombre": "Ana", "email": "ana@email.com"}
                """;
        String response = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String clientId = objectMapper.readTree(response).get("id").asText();

        // Consumir un plato (pizza)
        mockMvc.perform(post("/api/clients/{clientId}/dishes/{dishId}", clientId, pizza.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platosConsumidos", hasSize(1)))
                .andExpect(jsonPath("$.platosConsumidos[0]").value("Pizza"));

        // Consumir otro plato (ensalada)
        mockMvc.perform(post("/api/clients/{clientId}/dishes/{dishId}", clientId, ensalada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platosConsumidos", hasSize(2)));
    }

    @Test
    @DisplayName("JOIN: chef con más ventas retorna el chef correcto")
    void chefConMasVentas() throws Exception {
        // Crear dos clientes y asignarles platos del Chef Mario (más consumos)
        Client juan = Client.builder().nombre("Juan").email("juan@e.com").platosConsumidos(new ArrayList<>()).build();
        juan.getPlatosConsumidos().add(pizza);
        juan.getPlatosConsumidos().add(ensalada);
        clientRepository.save(juan);

        Client ana = Client.builder().nombre("Ana").email("ana@e.com").platosConsumidos(new ArrayList<>()).build();
        ana.getPlatosConsumidos().add(pizza);
        ana.getPlatosConsumidos().add(sushi);
        clientRepository.save(ana);

        // Chef Mario tiene 3 consumos (pizza*2 + ensalada*1), Chef Yoshi tiene 1 (sushi*1)
        mockMvc.perform(get("/api/clients/stats/chef-mas-vendido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Chef Mario"));
    }

    @Test
    @DisplayName("JOIN: platos de un cliente por chef retorna los platos correctos")
    void platosDeClientePorChef() throws Exception {
        // Juan consumió pizza y ensalada del Chef Mario
        Client juan = Client.builder().nombre("Juan").email("juan@e.com").platosConsumidos(new ArrayList<>()).build();
        juan.getPlatosConsumidos().add(pizza);
        juan.getPlatosConsumidos().add(ensalada);
        juan.getPlatosConsumidos().add(sushi);
        juan = clientRepository.save(juan);

        mockMvc.perform(get("/api/clients/{clientId}/chefs/{chefId}/platos", juan.getId(), chef1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nombre", containsInAnyOrder("Pizza", "Ensalada")));
    }

    @Test
    @DisplayName("JOIN: platos de un cliente por chef retorna vacío si no ha consumido de ese chef")
    void platosDeClientePorChefVacio() throws Exception {
        Client ana = Client.builder().nombre("Ana").email("ana@e.com").platosConsumidos(new ArrayList<>()).build();
        ana.getPlatosConsumidos().add(pizza); // Solo del Chef Mario
        ana = clientRepository.save(ana);

        mockMvc.perform(get("/api/clients/{clientId}/chefs/{chefId}/platos", ana.getId(), chef2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
